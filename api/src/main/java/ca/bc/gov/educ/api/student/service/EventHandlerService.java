package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.mappers.StudentTwinMapper;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentEvent;
import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import ca.bc.gov.educ.api.student.repository.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.struct.Event;
import ca.bc.gov.educ.api.student.struct.Student;
import ca.bc.gov.educ.api.student.struct.StudentTwin;
import ca.bc.gov.educ.api.student.struct.StudentCreate;
import ca.bc.gov.educ.api.student.struct.StudentUpdate;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  /**
   * The constant NO_RECORD_SAGA_ID_EVENT_TYPE.
   */
  public static final String NO_RECORD_SAGA_ID_EVENT_TYPE = "no record found for the saga id and event type combination, processing.";
  /**
   * The constant RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE.
   */
  public static final String RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE = "record found for the saga id and event type combination, might be a duplicate or replay," +
      " just updating the db status so that it will be polled and sent back again.";
  /**
   * The constant PAYLOAD_LOG.
   */
  public static final String PAYLOAD_LOG = "payload is :: {}";
  /**
   * The constant EVENT_PAYLOAD.
   */
  public static final String EVENT_PAYLOAD = "event is :: {}";
  @Getter(PRIVATE)
  private final StudentRepository studentRepository;
  private static final StudentMapper mapper = StudentMapper.mapper;
  private static final StudentTwinMapper twinMapper = StudentTwinMapper.mapper;
  @Getter(PRIVATE)
  private final StudentEventRepository studentEventRepository;

  @Getter(PRIVATE)
  private final StudentService studentService;


  @Getter(PRIVATE)
  private final StudentTwinService studentTwinService;

  /**
   * Instantiates a new Event handler service.
   *
   * @param studentRepository      the student repository
   * @param studentEventRepository the student event repository
   * @param studentService         the student service
   * @param studentTwinService     the student twin service
   */
  @Autowired
  public EventHandlerService(final StudentRepository studentRepository, final StudentEventRepository studentEventRepository, StudentService studentService, StudentTwinService studentTwinService) {
    this.studentRepository = studentRepository;
    this.studentEventRepository = studentEventRepository;
    this.studentService = studentService;
    this.studentTwinService = studentTwinService;
  }

  /**
   * Handle event.
   *
   * @param event the event
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleEvent(Event event) {
    try {
      switch (event.getEventType()) {
        case STUDENT_EVENT_OUTBOX_PROCESSED:
          log.info("received outbox processed event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleStudentOutboxProcessedEvent(event.getEventPayload());
          break;
        case GET_STUDENT:
          log.info("received get student event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleGetStudentEvent(event);
          break;
        case CREATE_STUDENT:
          log.info("received create student event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleCreateStudentEvent(event);
          break;
        case UPDATE_STUDENT:
          log.info("received update student event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleUpdateStudentEvent(event);
          break;
        case ADD_STUDENT_TWINS:
          log.info("received ADD_STUDENT_TWINS event :: ");
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          handleAddStudentTwins(event);
          break;
        default:
          log.info("silently ignoring other events.");
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  /**
   * This messaging handler expects clients to send array of twins.
   * Also it is expected that the payload given contains valid data.
   * <b> The clients responsibility is to provide valid payload in messaging flow.</b>
   *
   * @param event the event
   * @throws JsonProcessingException the json processing exception
   */
  protected void handleAddStudentTwins(Event event) throws JsonProcessingException {
    val studentEventOptional = getStudentEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    StudentEvent studentEvent;
    if (studentEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      final ObjectMapper objectMapper = new ObjectMapper();
      CollectionType javaType = objectMapper.getTypeFactory()
                                            .constructCollectionType(List.class, StudentTwin.class);
      List<StudentTwin> studentTwins = objectMapper.readValue(event.getEventPayload(), javaType);
      List<StudentTwinEntity> studentTwinEntities = studentTwins.stream().map(twinMapper::toModel).collect(Collectors.toList());
      getStudentTwinService().addStudentTwins(studentTwinEntities);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(studentTwinEntities.stream().map(twinMapper::toStructure).collect(Collectors.toList())));// need to convert to structure MANDATORY otherwise jackson will break.
      event.setEventOutcome(EventOutcome.STUDENT_TWINS_ADDED);
      studentEvent = createStudentEventRecord(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      studentEvent = studentEventOptional.get();
      studentEvent.setEventStatus(DB_COMMITTED.toString());
    }

    getStudentEventRepository().save(studentEvent);
  }

  private void handleUpdateStudentEvent(Event event) throws JsonProcessingException {
    val studentEventOptional = getStudentEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    StudentEvent studentEvent;
    if (studentEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      var student = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
      RequestUtil.setAuditColumns(student, false);
      try {
        val studentDBEntity = getStudentService().updateStudent(student);
        event.setEventPayload(JsonUtil.getJsonStringFromObject(mapper.toStructure(studentDBEntity)));// need to convert to structure MANDATORY otherwise jackson will break.
        event.setEventOutcome(EventOutcome.STUDENT_UPDATED);
      } catch (EntityNotFoundException ex) {
        event.setEventOutcome(EventOutcome.STUDENT_NOT_FOUND);
      }
      studentEvent = createStudentEventRecord(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      studentEvent = studentEventOptional.get();
      studentEvent.setEventStatus(DB_COMMITTED.toString());
    }

    getStudentEventRepository().save(studentEvent);
  }

  private void handleCreateStudentEvent(Event event) throws JsonProcessingException {
    val studentEventOptional = getStudentEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    StudentEvent studentEvent;
    if (studentEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      StudentCreate student = JsonUtil.getJsonObjectFromString(StudentCreate.class, event.getEventPayload());
      val optionalStudent = getStudentRepository().findStudentEntityByPen(student.getPen());
      if (optionalStudent.isPresent()) {
        event.setEventOutcome(EventOutcome.STUDENT_ALREADY_EXIST);
      } else {
        RequestUtil.setAuditColumns(student, true);
        StudentEntity entity;
        // It is expected that during messaging flow, the caller will provide a valid payload, so validation is not done.
        if (!CollectionUtils.isEmpty(student.getStudentMergeAssociations()) || !CollectionUtils.isEmpty(student.getStudentTwinAssociations())) {
          entity = getStudentService().createStudentWithAssociations(student);
        } else {
          entity = getStudentService().createStudent(mapper.toModel(student));
        }
        event.setEventOutcome(EventOutcome.STUDENT_CREATED);
        event.setEventPayload(JsonUtil.getJsonStringFromObject(mapper.toStructure(entity)));// need to convert to structure MANDATORY otherwise jackson will break.
      }
      studentEvent = createStudentEventRecord(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      studentEvent = studentEventOptional.get();
      studentEvent.setEventStatus(DB_COMMITTED.toString());
    }

    getStudentEventRepository().save(studentEvent);
  }

  private void handleStudentOutboxProcessedEvent(String studentEventId) {
    val studentEventFromDB = getStudentEventRepository().findById(UUID.fromString(studentEventId));
    if (studentEventFromDB.isPresent()) {
      val studEvent = studentEventFromDB.get();
      studEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
      getStudentEventRepository().save(studEvent);
    }
  }

  /**
   * Saga should never be null for this type of event.
   * this method expects that the event payload contains a pen number.
   *
   * @param event containing the student PEN.
   * @throws JsonProcessingException the json processing exception
   */
  public void handleGetStudentEvent(Event event) throws JsonProcessingException {
    val studentEventOptional = getStudentEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    StudentEvent studentEvent;
    if (studentEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      val optionalStudentEntity = getStudentRepository().findStudentEntityByPen(event.getEventPayload());
      if (optionalStudentEntity.isPresent()) {
        Student student = mapper.toStructure(optionalStudentEntity.get()); // need to convert to structure MANDATORY otherwise jackson will break.
        event.setEventPayload(JsonUtil.getJsonStringFromObject(student));
        event.setEventOutcome(EventOutcome.STUDENT_FOUND);
      } else {
        event.setEventOutcome(EventOutcome.STUDENT_NOT_FOUND);
      }
      studentEvent = createStudentEventRecord(event);
    } else { // just update the status of the event so that it will be polled and send again to the saga orchestrator.
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      studentEvent = studentEventOptional.get();
      studentEvent.setEventStatus(DB_COMMITTED.toString());
    }
    getStudentEventRepository().save(studentEvent);
  }

  private StudentEvent createStudentEventRecord(Event event) {
    return StudentEvent.builder()
                       .createDate(LocalDateTime.now())
                       .updateDate(LocalDateTime.now())
                       .createUser(event.getEventType().toString()) //need to discuss what to put here.
                       .updateUser(event.getEventType().toString())
                       .eventPayload(event.getEventPayload())
                       .eventType(event.getEventType().toString())
                       .sagaId(event.getSagaId())
                       .eventStatus(DB_COMMITTED.toString())
                       .eventOutcome(event.getEventOutcome().toString())
                       .replyChannel(event.getReplyTo())
                       .build();
  }
}
