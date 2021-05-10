package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.constant.Topics;
import ca.bc.gov.educ.api.student.filter.FilterOperation;
import ca.bc.gov.educ.api.student.mappers.v1.StudentHistoryMapper;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentRepository;
import ca.bc.gov.educ.api.student.service.v1.EventHandlerService;
import ca.bc.gov.educ.api.student.struct.v1.*;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.DateUtil;
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

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.student.constant.EventOutcome.*;
import static ca.bc.gov.educ.api.student.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.student.constant.EventType.*;
import static ca.bc.gov.educ.api.student.struct.v1.Condition.AND;
import static ca.bc.gov.educ.api.student.struct.v1.Condition.OR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class EventHandlerServiceTest {

  /**
   * The constant SEARCH_CRITERIA_LIST.
   */
  public static final String SEARCH_CRITERIA_LIST = "searchCriteriaList";
  /**
   * The constant PAGE_SIZE.
   */
  public static final String PAGE_SIZE = "pageSize";
  public static final String STUDENT_API_TOPIC = Topics.STUDENT_API_TOPIC.toString();
  @Autowired
  private StudentRepository studentRepository;
  @Autowired
  private StudentHistoryRepository studentHistoryRepository;
  @Autowired
  private StudentEventRepository studentEventRepository;

  @Autowired
  private EventHandlerService eventHandlerServiceUnderTest;
  private static final StudentMapper studentMapper = StudentMapper.mapper;
  private static final StudentHistoryMapper studentHistoryMapper = StudentHistoryMapper.mapper;
  private final boolean isSynchronous = false;
  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() {
    studentRepository.deleteAll();
    studentEventRepository.deleteAll();
    studentHistoryRepository.deleteAll();
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenNoStudentExist_shouldHaveEventOutcomeSTUDENT_NOT_FOUND() throws JsonProcessingException, IOException {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(UUID.randomUUID().toString()).build();
    byte[] response = eventHandlerServiceUnderTest.handleGetStudentEvent(event, isSynchronous);
    assertThat(response).isNotEmpty();
    Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_NOT_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT_HISTORY__whenNoStudentExist_shouldHaveEventOutcomeSTUDENT_HISTORY_NOT_FOUND() throws IOException {
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT_HISTORY).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(UUID.randomUUID().toString()).build();
    val responseEventBytes = eventHandlerServiceUnderTest.handleGetStudentHistoryEvent(event);
    val responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, responseEventBytes);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_HISTORY_NOT_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentExist_shouldHaveEventOutcomeSTUDENT_FOUND() throws JsonProcessingException, IOException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(entity.getPen()).build();
    byte[] response = eventHandlerServiceUnderTest.handleGetStudentEvent(event, isSynchronous);
    assertThat(response).isNotEmpty();
    Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT_HISTORY__whenStudentExist_shouldHaveEventOutcomeSTUDENT_HISTORY_FOUND() throws IOException {
    StudentEntity student = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    StudentHistory history = getStudentHistoryEntityFromJsonString(student.getStudentID().toString(), Optional.empty());

    StudentHistoryEntity entity = studentHistoryRepository.save(studentHistoryMapper.toModel(history));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT_HISTORY).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(entity.getStudentID().toString()).build();
    val responseEventBytes = eventHandlerServiceUnderTest.handleGetStudentHistoryEvent(event);
    val responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, responseEventBytes);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_HISTORY_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentExistAndDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_FOUND() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));

    var sagaId = UUID.randomUUID();
    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(GET_STUDENT.toString()).eventOutcome(STUDENT_FOUND.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(entity.getPen()).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);

    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(entity.getPen()).build();
    eventHandlerServiceUnderTest.handleGetStudentEvent(event, isSynchronous);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentExistAndSynchronousNatsMessage_shouldRespondWithStudentData() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var studentBytes = JsonUtil.getJsonBytesFromObject(studentMapper.toStructure(entity));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).eventPayload(entity.getPen()).build();
    var response = eventHandlerServiceUnderTest.handleGetStudentEvent(event, true);
    assertThat(studentBytes).isEqualTo(response);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENT__whenStudentDoesNotExistAndSynchronousNatsMessage_shouldRespondWithBlankObjectData() throws JsonProcessingException {
    var studentBytes = new byte[0];
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_STUDENT).sagaId(sagaId).eventPayload("123456789").build();
    var response = eventHandlerServiceUnderTest.handleGetStudentEvent(event, true);
    assertThat(studentBytes).isEqualTo(response);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_PAGINATED_STUDENT_BY_CRITERIA__whenStudentDoesNotExistAndSynchronousNatsMessage_shouldRespondWithBlankObjectData() throws IOException, ExecutionException, InterruptedException {
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });

    SearchCriteria criteriaFirstName = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.CONTAINS).value("a").valueType(ValueType.STRING).build();
    SearchCriteria criteriaLastName = SearchCriteria.builder().condition(AND).key("legalLastName").operation(FilterOperation.CONTAINS).value("l").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new LinkedList<>();
    criteriaList.add(criteriaFirstName);
    criteriaList.add(criteriaLastName);

    String fromDate = "1990-04-01";
    String toDate = "2020-04-15";
    SearchCriteria dobCriteria = SearchCriteria.builder().key("dob").operation(FilterOperation.BETWEEN).value(fromDate + "," + toDate).valueType(ValueType.DATE).build();
    List<SearchCriteria> criteriaList1 = new LinkedList<>();
    criteriaList1.add(dobCriteria);

    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaList1).build());

    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    studentRepository.saveAll(entities.stream().map(studentMapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_PAGINATED_STUDENT_BY_CRITERIA).sagaId(sagaId).eventPayload(SEARCH_CRITERIA_LIST.concat("=").concat(URLEncoder.encode(criteriaJSON, StandardCharsets.UTF_8)).concat("&").concat(PAGE_SIZE).concat("=").concat("100000").concat("&pageNumber=0")).build();
    var response = eventHandlerServiceUnderTest.handleGetPaginatedStudent(event).get();
    assertThat(response).hasSizeGreaterThan(3000);
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
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("USEREDIT");
    BeanUtils.copyProperties(studentMapper.toStructure(entity), studentUpdate);
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate)).build();
    eventHandlerServiceUnderTest.handleUpdateStudentEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_STUDENT.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_UPDATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT__whenStudentExistAndDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENT_UPDATED() throws JsonProcessingException, IOException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("MERGE");
    BeanUtils.copyProperties(studentMapper.toStructure(entity), studentUpdate);

    var sagaId = UUID.randomUUID();
    var penRequestEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(UPDATE_STUDENT.toString()).eventOutcome(STUDENT_UPDATED.toString()).
      eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(JsonUtil.getJsonStringFromObject(studentMapper.toStructure(entity))).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(penRequestEvent);

    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate)).build();
    Pair<byte[], StudentEvent> response = eventHandlerServiceUnderTest.handleUpdateStudentEvent(event);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response.getLeft());
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_UPDATED);
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT__whenEventExistsWithDifferentStudentInSagaMessage_shouldHaveEventOutcomeSTUDENT_UPDATED() throws JsonProcessingException, IOException {
    var studentEntity1 = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString(Optional.of("127054022"))));
    var studentEntity2 = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(studentEntity2.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("MERGE");
    BeanUtils.copyProperties(studentMapper.toStructure(studentEntity2), studentUpdate);

    var sagaId = UUID.randomUUID();
    var penRequestEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(UPDATE_STUDENT.toString()).eventOutcome(STUDENT_UPDATED.toString()).
            eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(JsonUtil.getJsonStringFromObject(studentMapper.toStructure(studentEntity1))).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(penRequestEvent);

    final Event event = Event.builder().eventType(UPDATE_STUDENT).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(studentUpdate)).build();
    Pair<byte[], StudentEvent> response = eventHandlerServiceUnderTest.handleUpdateStudentEvent(event);
    assertThat(response).isNotNull();
    Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response.getLeft());
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENT_UPDATED);
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE_STUDENT_and_InvalidHistoryActivityCode_shouldThrowException() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID().toString());
    BeanUtils.copyProperties(studentMapper.toStructure(entity), studentUpdate);
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
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_HISTORY_whenStudentDoNotExist_shouldHaveEventOutcomeSTUDENT_HISTORY_CREATED() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    StudentHistory history = getStudentHistoryEntityFromJsonString(entity.getStudentID().toString(), Optional.empty());

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT_HISTORY).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(Arrays.asList(history))).build();
    eventHandlerServiceUnderTest.handleCreateStudentHistoryEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT_HISTORY.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_HISTORY_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_HISTORY_whenStudentDoNotExist_shouldHaveEventOutcomeSTUDENT_HISTORY_CREATED_and_retainTheGivenCreateDate() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    StudentHistory history = getStudentHistoryEntityFromJsonString(entity.getStudentID().toString(), Optional.empty());
    history.setCreateDate(LocalDateTime.of(2000, 6,1, 7, 30, 45).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT_HISTORY).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(Arrays.asList(history))).build();
    eventHandlerServiceUnderTest.handleCreateStudentHistoryEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT_HISTORY.toString());
    assertThat(studentEventUpdated).isPresent();
    List<StudentHistory> payload = new ObjectMapper().readValue(studentEventUpdated.get().getEventPayload(), new TypeReference<>() {
    });
    assertThat(payload.size()).isEqualTo(1);
    assertThat(payload.get(0).getCreateDate()).isEqualTo(history.getCreateDate());
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_HISTORY_CREATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_HISTORY_whenStudentDoNotExist_shouldHaveEventOutcomeSTUDENT_HISTORY_ALREADY_EXISTS() throws JsonProcessingException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    StudentHistory history = getStudentHistoryEntityFromJsonString(entity.getStudentID().toString(), Optional.ofNullable(UUID.randomUUID().toString()));

    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(CREATE_STUDENT_HISTORY).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(Arrays.asList(history))).build();
    eventHandlerServiceUnderTest.handleCreateStudentHistoryEvent(event);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, CREATE_STUDENT_HISTORY.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENT_HISTORY_ALREADY_EXIST.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeCREATE_STUDENT_whenStudentDoNotExist_and_hasTwinAndMergeStudent_shouldHaveEventOutcomeSTUDENT_CREATED() throws JsonProcessingException {
    var twinStudent = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString(Optional.of("127054022"))));
    var mergeStudent = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString(Optional.of("127054023"))));
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
  public void testHandleEvent_givenEventTypeGET_STUDENTS__whenNoStudentExist_shouldHaveEventOutcomeSTUDENTS_NOT_FOUND() throws JsonProcessingException, IOException {
    var sagaId = UUID.randomUUID();
    List<UUID> ids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
    final Event event = Event.builder().eventType(GET_STUDENTS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(ids)).build();
    byte[] response = eventHandlerServiceUnderTest.handleGetStudentsEvent(event);
    assertThat(response).isNotEmpty();
    Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENTS_NOT_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENTS__whenSomeStudentExist_shouldHaveEventOutcomeSTUDENTS_NOT_FOUND() throws IOException {
    StudentEntity entity = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var sagaId = UUID.randomUUID();
    List<UUID> ids = Arrays.asList(entity.getStudentID(), UUID.randomUUID());
    final Event event = Event.builder().eventType(GET_STUDENTS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(ids)).build();
    byte[] response = eventHandlerServiceUnderTest.handleGetStudentsEvent(event);
    assertThat(response).isNotEmpty();
    Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENTS_NOT_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENTS__whenStudentsExist_shouldHaveEventOutcomeSTUDENTS_FOUND() throws IOException {
    StudentEntity entity1 = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    StudentEntity entity2 = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var sagaId = UUID.randomUUID();
    List<UUID> ids = Arrays.asList(entity1.getStudentID(), entity2.getStudentID());
    final Event event = Event.builder().eventType(GET_STUDENTS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(ids)).build();
    byte[] response = eventHandlerServiceUnderTest.handleGetStudentsEvent(event);
    assertThat(response).isNotEmpty();
    Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
    assertThat(responseEvent).isNotNull();
    assertThat(responseEvent.getEventOutcome()).isEqualTo(STUDENTS_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET_STUDENTS__whenStudentExistAndDuplicateSagaMessage_shouldHaveEventOutcomeSTUDENTS_FOUND() throws JsonProcessingException {
    StudentEntity entity1 = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    StudentEntity entity2 = studentRepository.save(studentMapper.toModel(getStudentEntityFromJsonString()));
    var sagaId = UUID.randomUUID();
    List<UUID> ids = Arrays.asList(entity1.getStudentID(), entity2.getStudentID());
    var studentEvent = StudentEvent.builder().sagaId(sagaId).replyChannel(STUDENT_API_TOPIC).eventType(GET_STUDENTS.toString()).eventOutcome(STUDENTS_FOUND.toString()).
            eventStatus(MESSAGE_PUBLISHED.toString()).eventPayload(JsonUtil.getJsonStringFromObject(ids)).createDate(LocalDateTime.now()).createUser("TEST").build();
    studentEventRepository.save(studentEvent);

    final Event event = Event.builder().eventType(GET_STUDENTS).sagaId(sagaId).replyTo(STUDENT_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(ids)).build();
    eventHandlerServiceUnderTest.handleGetStudentEvent(event, isSynchronous);
    var studentEventUpdated = studentEventRepository.findBySagaIdAndEventType(sagaId, GET_STUDENTS.toString());
    assertThat(studentEventUpdated).isPresent();
    assertThat(studentEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(studentEventUpdated.get().getEventOutcome()).isEqualTo(STUDENTS_FOUND.toString());
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

  private StudentHistory getStudentHistoryEntityFromJsonString(String studentID, Optional<String> studentHistoryID) {
    try {
      return new ObjectMapper().readValue(placeHolderStudentHistoryJSON(studentID, studentHistoryID), StudentHistory.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String placeHolderStudentHistoryJSON(String studentID, Optional<String> studentHistoryID) {
    return placeHolderStudentHistoryJSON(studentID, studentHistoryID, Optional.of("USERNEW"));
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

  protected String placeHolderStudentHistoryJSON(String studentID, Optional<String> studentHistoryID, Optional<String> historyActivityCode) {
    return "{\"studentID\":\"" + studentID + "\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"sexCode\":\"M\"," +
            "\"statusCode\":\"A\",\"demogCode\":\"A\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"," +
            "\"pen\":\"127054021\"," + (studentHistoryID.isPresent() ? "\"studentHistoryID\":\"" + studentHistoryID.get() + "\"," : "") +
            (historyActivityCode.isPresent() ? "\"historyActivityCode\":\"" + historyActivityCode.get() + "\"}" : "") ;
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
