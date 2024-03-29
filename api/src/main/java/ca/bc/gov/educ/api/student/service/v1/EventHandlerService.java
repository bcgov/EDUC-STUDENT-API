package ca.bc.gov.educ.api.student.service.v1;

import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;
import static lombok.AccessLevel.PRIVATE;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.mappers.v1.StudentHistoryMapper;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentRepository;
import ca.bc.gov.educ.api.student.struct.v1.Event;
import ca.bc.gov.educ.api.student.struct.v1.Student;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistory;
import ca.bc.gov.educ.api.student.struct.v1.StudentUpdate;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
@SuppressWarnings("java:S3864")
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
  @Getter(PRIVATE)
  private final StudentHistoryRepository studentHistoryRepository;

  private static final StudentMapper studentMapper = StudentMapper.mapper;
  private static final StudentHistoryMapper studentHistoryMapper = StudentHistoryMapper.mapper;
  /**
   * The Ob mapper.
   */
  private final ObjectMapper obMapper = new ObjectMapper();

  @Getter(PRIVATE)
  private final StudentEventRepository studentEventRepository;

  @Getter(PRIVATE)
  private final StudentService studentService;

  @Getter(PRIVATE)
  private final StudentHistoryService studentHistoryService;

  @Getter(PRIVATE)
  private final StudentSearchService studentSearchService;

  /**
   * The constant SEARCH_CRITERIA_LIST.
   */
  public static final String SEARCH_CRITERIA_LIST = "searchCriteriaList";
  /**
   * The constant PAGE_SIZE.
   */
  public static final String PAGE_SIZE = "pageSize";

  /**
   * The constant PAGE_SIZE.
   */
  public static final String PAGE_NUMBER = "pageNumber";

  /**
   * The constant PAGE_SIZE.
   */
  public static final String SORT_CRITERIA = "sortCriteriaJson";

  /**
   * Instantiates a new Event handler service.
   *
   * @param studentRepository      the student repository
   * @param studentEventRepository the student event repository
   * @param studentService         the student service
   * @param studentSearchService   the student search service
   */
  @Autowired
  public EventHandlerService(final StudentRepository studentRepository, final StudentEventRepository studentEventRepository, final StudentHistoryRepository studentHistoryRepository,final StudentHistoryService studentHistoryService, StudentService studentService, StudentSearchService studentSearchService) {
    this.studentRepository = studentRepository;
    this.studentEventRepository = studentEventRepository;
    this.studentService = studentService;
    this.studentSearchService = studentSearchService;
    this.studentHistoryRepository = studentHistoryRepository;
    this.studentHistoryService = studentHistoryService;
  }


  /**
   * Handle update student event byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<byte[], StudentEvent> handleUpdateStudentEvent(Event event) throws JsonProcessingException {
    StudentEvent choreographyEvent = null;
    log.trace(EVENT_PAYLOAD, event);
    var student = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
    RequestUtil.setAuditColumnsForCreate(student);
    try {
      val pair = getStudentService().updateStudent(student, UUID.fromString(student.getStudentID()));
      choreographyEvent = pair.getRight();
      event.setEventPayload(JsonUtil.getJsonStringFromObject(studentMapper.toStructure(pair.getLeft())));// need to convert to structure MANDATORY otherwise jackson will break.
      event.setEventOutcome(EventOutcome.STUDENT_UPDATED);
    } catch (EntityNotFoundException ex) {
      event.setEventOutcome(EventOutcome.STUDENT_NOT_FOUND);
    }

    val studentEvent = createStudentEventRecord(event);
    getStudentEventRepository().save(studentEvent);
    return Pair.of(createResponseEvent(studentEvent), choreographyEvent);
  }

  /**
   * Handle create student event byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<byte[], StudentEvent> handleCreateStudentEvent(Event event) throws JsonProcessingException {
    val studentEventOptional = getStudentEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    StudentEvent studentEvent;
    StudentEvent choreographyEvent = null;
    if (studentEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      StudentCreate student = JsonUtil.getJsonObjectFromString(StudentCreate.class, event.getEventPayload());
      val optionalStudent = getStudentRepository().findStudentEntityByPen(student.getPen());
      if (optionalStudent.isPresent()) {
        event.setEventOutcome(EventOutcome.STUDENT_ALREADY_EXIST);
        event.setEventPayload(optionalStudent.get().getStudentID().toString()); // return the student ID in response.
      } else {
        RequestUtil.setAuditColumnsForCreate(student);
        var studentPair = getStudentService().createStudent(student);
        StudentEntity entity = studentPair.getLeft();
        choreographyEvent = studentPair.getRight();
        event.setEventOutcome(EventOutcome.STUDENT_CREATED);
        event.setEventPayload(JsonUtil.getJsonStringFromObject(studentMapper.toStructure(entity)));// need to convert to structure MANDATORY otherwise jackson will break.
      }
      studentEvent = createStudentEventRecord(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      studentEvent = studentEventOptional.get();
      studentEvent.setUpdateDate(LocalDateTime.now());
    }

    getStudentEventRepository().save(studentEvent);
    return Pair.of(createResponseEvent(studentEvent), choreographyEvent);
  }


  /**
   * Saga should never be null for this type of event.
   * this method expects that the event payload contains a pen number.
   *
   * @param event         containing the student PEN.
   * @param isSynchronous the is synchronous
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public byte[] handleGetStudentEvent(Event event, boolean isSynchronous) throws JsonProcessingException {
    if (isSynchronous) {
      val optionalStudentEntity = getStudentRepository().findStudentEntityByPen(event.getEventPayload());
      if (optionalStudentEntity.isPresent()) {
        return JsonUtil.getJsonBytesFromObject(studentMapper.toStructure(optionalStudentEntity.get()));
      } else {
        return new byte[0];
      }
    }

    log.trace(EVENT_PAYLOAD, event);
    val optionalStudentEntity = getStudentRepository().findStudentEntityByPen(event.getEventPayload());
    if (optionalStudentEntity.isPresent()) {
      Student student = studentMapper.toStructure(optionalStudentEntity.get()); // need to convert to structure MANDATORY otherwise jackson will break.
      event.setEventPayload(JsonUtil.getJsonStringFromObject(student));
      event.setEventOutcome(EventOutcome.STUDENT_FOUND);
    } else {
      event.setEventOutcome(EventOutcome.STUDENT_NOT_FOUND);
    }
    val studentEvent = createStudentEventRecord(event);
    return createResponseEvent(studentEvent);
  }

  /**
   * Saga should never be null for this type of event.
   * this method expects that the event payload contains a list of pen numbers.
   *
   * @param event         containing the student PENs.
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public byte[] handleGetStudentsEvent(Event event) throws JsonProcessingException {
    Set<UUID> studentIds = obMapper.readValue(event.getEventPayload(), new TypeReference<>() {
    }); // typecast to a set to eliminate duplicates.
    var partitionedList = Iterables.partition(studentIds, 900);
    List<StudentEntity> studentEntityList = new ArrayList<>();
    for (List<UUID> list : partitionedList) {
      studentEntityList.addAll(getStudentRepository().findStudentEntityByStudentIDIn(list));
    }
    if (!studentEntityList.isEmpty() && studentEntityList.size() == studentIds.size()) {
      var studentList = studentEntityList.stream().map(studentMapper::toStructure).toList();
      event.setEventPayload(JsonUtil.getJsonStringFromObject(studentList));
      event.setEventOutcome(EventOutcome.STUDENTS_FOUND);
    } else {
      event.setEventOutcome(EventOutcome.STUDENTS_NOT_FOUND);
    }
    log.info("Found :: {} unique students, Event outcome for saga ID :: {}, is :: {}", studentEntityList.size(), event.getSagaId(), event.getEventOutcome());
    return createResponseEvent(createStudentEventRecord(event));
  }

  /**
   * Handle create student history event byte [ ].
   *  => External interface to copy audit history so that copyAudit flag should be true to keep createDate and createUser audit info.
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public byte[] handleCreateStudentHistoryEvent(Event event) throws JsonProcessingException {
    val studentEventOptional = getStudentEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    StudentEvent studentEvent;
    if (studentEventOptional.isEmpty()) {
      List<StudentHistory> studentList = new ArrayList<>();
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      JavaType type = obMapper.getTypeFactory().
              constructCollectionType(List.class, StudentHistory.class);
      List<StudentHistory> audits = obMapper.readValue(event.getEventPayload(), type);
      audits.forEach(studentHistory -> {
        if (StringUtils.isBlank(studentHistory.getStudentHistoryID())) {
          RequestUtil.setAuditColumnsForCreateIfBlank(studentHistory);
          StudentHistoryEntity entity = getStudentHistoryService().createStudentHistory(studentHistory, true);
          studentList.add(studentHistoryMapper.toStructure(entity));
        }
      });

      if (studentList.isEmpty()) {
        event.setEventOutcome(EventOutcome.STUDENT_HISTORY_ALREADY_EXIST);
        event.setEventPayload(JsonUtil.getJsonStringFromObject(audits.stream().map(StudentHistory::getStudentHistoryID).toList()));
      } else {
        event.setEventOutcome(EventOutcome.STUDENT_HISTORY_CREATED);
        event.setEventPayload(JsonUtil.getJsonStringFromObject(studentList));// need to convert to s
      }

      studentEvent = createStudentEventRecord(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      studentEvent = studentEventOptional.get();
      studentEvent.setUpdateDate(LocalDateTime.now());
    }

    getStudentEventRepository().save(studentEvent);
    return createResponseEvent(studentEvent);
  }


  /**
   * Saga should never be null for this type of event.
   * this method expects that the event payload contains a pen number.
   *
   * @param event         containing the student PEN.
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public byte[] handleGetStudentHistoryEvent(Event event) throws JsonProcessingException {
    val studentHistoryEntityList = getStudentHistoryRepository().findByStudentID(UUID.fromString(event.getEventPayload()));
    log.info("Found :: {} student history records for saga ID :: {}", studentHistoryEntityList.size(), event.getSagaId());
    if (!studentHistoryEntityList.isEmpty()) {
      var studentHistoryList = studentHistoryEntityList.stream().map(studentHistoryMapper::toStructure).collect(Collectors.toList());
      event.setEventPayload(JsonUtil.getJsonStringFromObject(studentHistoryList));
      event.setEventOutcome(EventOutcome.STUDENT_HISTORY_FOUND);
    } else {
      event.setEventOutcome(EventOutcome.STUDENT_HISTORY_NOT_FOUND);
    }
    log.info("Event outcome for saga ID :: {}, is :: {}", event.getSagaId(), event.getEventOutcome());
    return createResponseEvent(createStudentEventRecord(event));
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
        .eventStatus(MESSAGE_PUBLISHED.toString())
        .eventOutcome(event.getEventOutcome().toString())
        .replyChannel(event.getReplyTo())
        .build();
  }

  private byte[] createResponseEvent(StudentEvent event) throws JsonProcessingException {
    val responseEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(EventType.valueOf(event.getEventType()))
        .eventOutcome(EventOutcome.valueOf(event.getEventOutcome()))
        .eventPayload(event.getEventPayload()).build();
    return JsonUtil.getJsonBytesFromObject(responseEvent);
  }

  /**
   * Handle get paginated student byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   */
  public CompletableFuture<byte[]> handleGetPaginatedStudent(Event event) {
    String sortCriteriaJson = null;
    String searchCriteriaListJson = null;
    var pageNumber = 0;
    var pageSize = 100000;
    var params = event.getEventPayload().split("&");
    for (String param : params) {
      if (param != null) {
        var keyValPair = param.split("=");
        if (SEARCH_CRITERIA_LIST.equalsIgnoreCase(keyValPair[0])) {
          searchCriteriaListJson = URLDecoder.decode(keyValPair[1], StandardCharsets.UTF_8);
        } else if (PAGE_SIZE.equalsIgnoreCase(keyValPair[0])) {
          pageSize = Integer.parseInt(keyValPair[1]);
        } else if (PAGE_NUMBER.equalsIgnoreCase(keyValPair[0])) {
          pageNumber = Integer.parseInt(keyValPair[1]);
        } else if (SORT_CRITERIA.equalsIgnoreCase(keyValPair[0])) {
          sortCriteriaJson = keyValPair[1];
        }
      }
    }

    final List<Sort.Order> sorts = new ArrayList<>();
    Specification<StudentEntity> studentSpecs = studentSearchService.setSpecificationAndSortCriteria(sortCriteriaJson, searchCriteriaListJson, obMapper, sorts);
    return getStudentService()
        .findAll(studentSpecs, pageNumber, pageSize, sorts)
        .thenApplyAsync(studentEntities -> studentEntities.map(studentMapper::toStructure))
        .thenApplyAsync(studentEntities -> {
          try {
            log.info("found {} students for {}", studentEntities.getContent().size(), event.getSagaId());
            val resBytes = obMapper.writeValueAsBytes(studentEntities);
            log.info("response prepared for {}, response length {}", event.getSagaId(), resBytes.length);
            return resBytes;
          } catch (JsonProcessingException e) {
            log.error("Error during get paginated student :: {} {}", event, e);
          }
          return new byte[0];
        });

  }


}
