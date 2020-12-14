package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.constant.Topics;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.mappers.StudentTwinMapper;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentEvent;
import ca.bc.gov.educ.api.student.repository.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.repository.StudentTwinRepository;
import ca.bc.gov.educ.api.student.struct.Event;
import ca.bc.gov.educ.api.student.struct.Student;
import ca.bc.gov.educ.api.student.struct.StudentTwin;
import ca.bc.gov.educ.api.student.struct.StudentUpdate;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.student.constant.EventOutcome.*;
import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.student.constant.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() {
    studentTwinRepository.deleteAll();
    studentMergeRepository.deleteAll();
    studentRepository.deleteAll();
    studentEventRepository.deleteAll();
  }


  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenNoStudentExist_shouldHaveEventOutcomeSTUDENT_NOT_FOUND() throws JsonProcessingException {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(UUID.randomUUID().toString()).build();
    eventHandlerServiceUnderTest.handleGetStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_NOT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentExist_shouldHaveEventOutcomeSTUDENT_FOUND() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(entity.getPen()).build();
    eventHandlerServiceUnderTest.handleGetStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentExistAndDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_FOUND() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));

    var sagaId = UUID.randomUUID();
    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(GET_STUDENT.toString()).eventOutcome(STUDENT_FOUND.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(entity.getPen()).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);

    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(entity.getPen()).build();
    eventHandlerServiceUnderTest.handleGetStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT__whenStudentDoNotExist_shouldHaveEventOutcomeSTUDENT_NOT_FOUND() throws JsonProcessingException {
    Student entity = getStudentEntityFromJsonString();
    entity.setStudentID(UUID.randomUUID().toString());
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID());
    studentUpdate.setHistoryActivityCode("USEREDIT");
    BeanUtils.copyProperties(entity, studentUpdate);
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate)).build();
    eventHandlerServiceUnderTest.handleUpdateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_NOT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT__whenStudentExist_shouldHaveEventOutcomeSTUDENT_UPDATED() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("USEREDIT");
    BeanUtils.copyProperties(mapper.toStructure(entity), studentUpdate);
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate)).build();
    eventHandlerServiceUnderTest.handleUpdateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
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
    eventHandlerServiceUnderTest.handleUpdateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_UPDATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT_and_InvalidHistoryActivityCode_shouldThrowException() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString()));
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID().toString());
    BeanUtils.copyProperties(mapper.toStructure(entity), studentUpdate);
    studentUpdate.setHistoryActivityCode(null);
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate)).build();
    assertThrows(TransactionSystemException.class, () -> eventHandlerServiceUnderTest.handleUpdateStudentEvent(event));
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenStudentDoNotExist_shouldHaveEventOutcomeSTUDENT_CREATED() throws JsonProcessingException {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(placeHolderStudentJSON()).build();
    eventHandlerServiceUnderTest.handleCreateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenStudentDoNotExist_and_hasTwinAndMergeStudent_shouldHaveEventOutcomeSTUDENT_CREATED() throws JsonProcessingException {
    var twinStudent = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("127054022"))));
    var mergeStudent = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("127054023"))));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(
      placeHolderStudentJSON(Optional.of(twinStudent.getStudentID().toString()), Optional.of(mergeStudent.getStudentID().toString()), Optional.empty())).build();
    eventHandlerServiceUnderTest.handleCreateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_and_InvalidHistoryActivityCode_shouldThrowException() {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC)
      .eventPayload(placeHolderStudentJSONWithHistoryActivityCode(Optional.empty())).build();
    assertThrows(TransactionSystemException.class, () -> eventHandlerServiceUnderTest.handleCreateStudentEvent(event));
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenStudentExist_shouldHaveEventOutcomeSTUDENT_ALREADY_EXIST() throws JsonProcessingException {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(placeHolderStudentJSON()).build();
    eventHandlerServiceUnderTest.handleCreateStudentEvent(event);
    sagaId = UUID.randomUUID();
    event.setSagaId(sagaId);
    eventHandlerServiceUnderTest.handleCreateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_ALREADY_EXIST.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_CREATED() throws JsonProcessingException {
    var sagaId = UUID.randomUUID();

    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(CREATE_STUDENT.toString()).eventOutcome(STUDENT_CREATED.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(placeHolderStudentJSON()).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);

    final Event event = Event.builder().eventType(CREATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(placeHolderStudentJSON()).build();
    eventHandlerServiceUnderTest.handleCreateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString()); // the db status is updated from  MESSAGE_PUBLISHED
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeADD_STUDENT_TWINS_shouldOnlyUpdateTheStatus() throws JsonProcessingException {
    var twinStudent = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("127054022"))));
    var twinStudent2 = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("120164447"))));
    List<StudentTwin> studentTwins = new ArrayList<>();
    studentTwins.add(StudentTwin.builder()
      .studentID(twinStudent.getStudentID().toString())
      .twinStudentID(twinStudent2.getStudentID().toString())
      .studentTwinReasonCode("PENCREATE")
      .createUser("TEST_USER")
      .updateUser("TEST_USER")
      .updateDate(LocalDateTime.now().toString())
      .createDate(LocalDateTime.now().toString()).build());

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(ADD_STUDENT_TWINS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC)
      .eventPayload(JsonUtil.getJsonStringFromObject(studentTwins)).build();
    eventHandlerServiceUnderTest.handleAddStudentTwins(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, ADD_STUDENT_TWINS.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_TWINS_ADDED.toString());
    var studentEvent = studentEventUpdated.get();
    studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
    studentEventRepository.save(studentEvent);
    eventHandlerServiceUnderTest.handleAddStudentTwins(event); // call the method again to simulate duplicate message.
    studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, ADD_STUDENT_TWINS.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_TWINS_ADDED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeADDSTUDENTTWINSDuplicateMessage_shouldCreateTwins() throws JsonProcessingException {
    var twinStudent = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("127054022"))));
    var twinStudent2 = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("120164447"))));
    List<StudentTwin> studentTwins = new ArrayList<>();
    studentTwins.add(StudentTwin.builder()
      .studentID(twinStudent.getStudentID().toString())
      .twinStudentID(twinStudent2.getStudentID().toString())
      .studentTwinReasonCode("PENCREATE")
      .createUser("TEST_USER")
      .updateUser("TEST_USER")
      .updateDate(LocalDateTime.now().toString())
      .createDate(LocalDateTime.now().toString()).build());

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(ADD_STUDENT_TWINS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC)
      .eventPayload(JsonUtil.getJsonStringFromObject(studentTwins)).build();
    eventHandlerServiceUnderTest.handleAddStudentTwins(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, ADD_STUDENT_TWINS.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_TWINS_ADDED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeDELETE_STUDENT_TWINS_shouldDeleteTwins() throws JsonProcessingException {
    var twinStudent = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("127054022"))));
    var twinStudent2 = studentRepository.save(mapper.toModel(getStudentEntityFromJsonString(Optional.of("120164447"))));
    List<StudentTwin> studentTwins = new ArrayList<>();
    studentTwins.add(StudentTwin.builder()
      .studentID(twinStudent.getStudentID().toString())
      .twinStudentID(twinStudent2.getStudentID().toString())
      .studentTwinReasonCode("PENCREATE")
      .createUser("TEST_USER")
      .updateUser("TEST_USER")
      .updateDate(LocalDateTime.now().toString())
      .createDate(LocalDateTime.now().toString()).build());
    studentTwinRepository.saveAll(studentTwins.stream().map(StudentTwinMapper.mapper::toModel).collect(Collectors.toList()));

    var studentTwinIDs = studentTwins.stream().map(StudentTwin::getStudentTwinID).collect(Collectors.toList());
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(DELETE_STUDENT_TWINS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC)
      .eventPayload(JsonUtil.getJsonStringFromObject(studentTwinIDs)).build();
    eventHandlerServiceUnderTest.handleDeleteStudentTwins(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, DELETE_STUDENT_TWINS.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_TWINS_DELETED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeDELETE_STUDENT_TWINS__whenDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_TWINS_DELETED() throws JsonProcessingException {
    var payload = JsonUtil.getJsonStringFromObject(List.of(UUID.randomUUID().toString()));
    var sagaId = UUID.randomUUID();
    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(DELETE_STUDENT_TWINS.toString()).eventOutcome(STUDENT_TWINS_DELETED.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(payload).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);

    final Event event = Event.builder().eventType(DELETE_STUDENT_TWINS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(payload).build();
    eventHandlerServiceUnderTest.handleDeleteStudentTwins(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, DELETE_STUDENT_TWINS.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_TWINS_DELETED.toString());
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
    return placeHolderStudentJSON(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("USERNEW"));
  }

  protected String placeHolderStudentJSON(Optional<String> pen) {
    return placeHolderStudentJSON(Optional.empty(), Optional.empty(), pen, Optional.of("USERNEW"));
  }

  protected String placeHolderStudentJSONWithHistoryActivityCode(Optional<String> historyActivityCode) {
    return placeHolderStudentJSON(Optional.empty(), Optional.empty(), Optional.empty(), historyActivityCode);
  }

  protected String placeHolderStudentJSON(Optional<String> twinStudentID, Optional<String> mergeStudentID, Optional<String> pen) {
    return placeHolderStudentJSON(twinStudentID, mergeStudentID, pen, Optional.of("USERNEW"));
  }

  protected String placeHolderStudentJSON(Optional<String> twinStudentID, Optional<String> mergeStudentID, Optional<String> pen, Optional<String> historyActivityCode) {
    return "{\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"sexCode\":\"M\"," +
      "\"statusCode\":\"A\",\"demogCode\":\"A\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"," +
      "\"pen\":\"" + pen.orElse("127054021") + "\"," + (historyActivityCode.isPresent() ? "\"historyActivityCode\":\"" + historyActivityCode.get() + "\"," : "") +
      "\"studentTwinAssociations\": [" +
      (twinStudentID.isPresent() ? "{\"twinStudentID\": \"" + twinStudentID.get() + "\", \"studentTwinReasonCode\": \"PENCREATE\"}" : "") +
      "], \"studentMergeAssociations\": [" +
      (mergeStudentID.isPresent() ? "{\"mergeStudentID\": \"" + mergeStudentID.get() + "\", \"studentMergeDirectionCode\": \"FROM\", \"studentMergeSourceCode\": \"MINISTRY\"}" : "") +
      "]}";
  }
}
