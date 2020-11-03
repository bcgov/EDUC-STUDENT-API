package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.constant.Topics;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.repository.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.model.StudentEvent;
import ca.bc.gov.educ.api.student.repository.StudentTwinRepository;
import ca.bc.gov.educ.api.student.struct.Event;
import ca.bc.gov.educ.api.student.struct.Student;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.student.constant.EventOutcome.*;
import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.student.constant.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class EventHandlerServiceTest {

  public static final String STUDENT_API_TOPIC = Topics.STUDENT_API_TOPIC.toString();
  @Autowired
  private StudentRepository studentRepository;
  @Autowired
  private StudentEventRepository studentEventRepository;
  @Autowired
  private StudentTwinRepository studentTwinRepository;
  @Autowired
  private StudentMergeRepository studentMergeRepository;

  @Autowired
  private EventHandlerService eventHandlerServiceUnderTest;
  private static final StudentMapper mapper = StudentMapper.mapper;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @After
  public void tearDown() {
    studentTwinRepository.deleteAll();
    studentMergeRepository.deleteAll();
    studentRepository.deleteAll();
    studentEventRepository.deleteAll();
  }

  @Test
  public void testHandleEvent_givenEventTypeSTUDENT_EVENT_OUTBOX_PROCESSED_shouldUpdateDBStatus() {
    var studentEvent = StudentEvent.builder().eventType(GET_STUDENT.toString()).eventOutcome(STUDENT_FOUND.toString()).eventStatus(DB_COMMITTED.toString()).
      eventPayload("{}").createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);
    var eventId = studentEvent.getEventId();
    final Event event = new Event(STUDENT_EVENT_OUTBOX_PROCESSED, null, null, eventId.toString(), eventId);
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findById(eventId);
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenNoStudentExist_shouldHaveEventOutcomeSTUDENT_NOT_FOUND() {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(UUID.randomUUID().toString()).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_NOT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentExist_shouldHaveEventOutcomeSTUDENT_FOUND() {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(entity.getPen()).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentExistAndDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_FOUND() {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));

    var sagaId = UUID.randomUUID();
    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(GET_STUDENT.toString()).eventOutcome(STUDENT_FOUND.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(entity.getPen()).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);

    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(entity.getPen()).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT__whenStudentDoNotExist_shouldHaveEventOutcomeSTUDENT_NOT_FOUND() throws JsonProcessingException {
    Student entity = getStudentEntityFromJsonString();
    entity.setStudentID(UUID.randomUUID().toString());
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(entity)).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_NOT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT__whenStudentExist_shouldHaveEventOutcomeSTUDENT_UPDATED() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(mapper.toStructure(entity))).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_UPDATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT__whenStudentExistAndDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_UPDATED() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));

    var sagaId = UUID.randomUUID();
    var penRequestEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(UPDATE_STUDENT.toString()).eventOutcome(STUDENT_UPDATED.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(entity.getPen()).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(penRequestEvent);

    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(entity)).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_UPDATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenStudentDoNotExist_shouldHaveEventOutcomeSTUDENT_CREATED() {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(placeHolderStudentJSON()).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenStudentDoNotExist_and_hasTwinAndMergeStudent_shouldHaveEventOutcomeSTUDENT_CREATED() {
    var twinStudent = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("127054022"))));
    var mergeStudent = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("127054023"))));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(
      placeHolderStudentJSON(Optional.of(twinStudent.getStudentID().toString()), Optional.of(mergeStudent.getStudentID().toString()), Optional.empty())).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenStudentExist_shouldHaveEventOutcomeSTUDENT_ALREADY_EXIST() {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(placeHolderStudentJSON()).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    sagaId = UUID.randomUUID();
    event.setSagaId(sagaId);
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_ALREADY_EXIST.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_CREATED() {
    var sagaId = UUID.randomUUID();

    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(CREATE_STUDENT.toString()).eventOutcome(STUDENT_CREATED.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(placeHolderStudentJSON()).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);

    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(placeHolderStudentJSON()).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(DB_COMMITTED.toString()); // the db status is updated from  MESSAGE_PUBLISHED
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeNULL_shouldIgnoreEvent() throws JsonProcessingException {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(null).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(placeHolderStudentJSON()).build();
    eventHandlerServiceUnderTest.handleEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isEmpty();
  }

  private Student getStudentEntityFromJsonString() {
    return getStudentEntityFromJsonString(Optional.empty());
  }

  private Student getStudentEntityFromJsonString(Optional<String> pen) {
    try {
      return new ObjectMapper().readValue(placeHolderStudentJSON(pen), Student.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String placeHolderStudentJSON() {
    return placeHolderStudentJSON(Optional.empty(), Optional.empty(), Optional.empty());
  }

  protected String placeHolderStudentJSON(Optional<String> pen) {
    return placeHolderStudentJSON(Optional.empty(), Optional.empty(), pen);
  }

  protected String placeHolderStudentJSON(Optional<String> twinStudentID, Optional<String> mergeStudentID, Optional<String> pen) {
    return "{\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"sexCode\":\"M\"," +
      "\"statusCode\":\"A\",\"demogCode\":\"A\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"," +
      "\"pen\":\"" + pen.orElse("127054021") + "\", \"studentTwinAssociations\": [" +
      (twinStudentID.isPresent() ? "{\"twinStudentID\": \"" + twinStudentID.get() + "\", \"studentTwinReasonCode\": \"PENCREATE\"}" : "") +
      "], \"studentMergeAssociations\": [" +
      (mergeStudentID.isPresent() ? "{\"mergeStudentID\": \"" + mergeStudentID.get() + "\", \"studentMergeDirectionCode\": \"FROM\", \"studentMergeSourceCode\": \"MINISTRY\"}" : "") +
      "]}";
  }
}
