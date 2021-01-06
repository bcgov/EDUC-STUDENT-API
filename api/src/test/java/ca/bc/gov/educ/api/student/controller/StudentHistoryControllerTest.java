package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.controller.v1.StudentHistoryController;
import ca.bc.gov.educ.api.student.filter.FilterOperation;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryActivityCodeEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.repository.v1.StudentHistoryActivityCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentRepository;
import ca.bc.gov.educ.api.student.struct.v1.Search;
import ca.bc.gov.educ.api.student.struct.v1.SearchCriteria;
import ca.bc.gov.educ.api.student.struct.v1.Student;
import ca.bc.gov.educ.api.student.struct.v1.ValueType;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.student.constant.v1.URL.*;
import static ca.bc.gov.educ.api.student.struct.v1.Condition.AND;
import static ca.bc.gov.educ.api.student.struct.v1.Condition.OR;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = StudentApiApplication.class)
@AutoConfigureMockMvc
public class StudentHistoryControllerTest {

  private static final StudentMapper mapper = StudentMapper.mapper;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  StudentHistoryController controller;

  @Autowired
  StudentRepository repository;

  @Autowired
  StudentHistoryActivityCodeTableRepository studentHistoryActivityCodeTableRepo;
  @Autowired
  StudentHistoryRepository studentHistoryRepo;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    studentHistoryActivityCodeTableRepo.save(createStudentHistoryActivityCodeData());
  }

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    studentHistoryActivityCodeTableRepo.deleteAll();
    studentHistoryRepo.deleteAll();
    repository.deleteAll();
  }

  @Test
  public void testReadStudentHistoryPaginated_givenStudentID_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntity(student, "USEREDIT", 2), createStudentHistoryEntity(student, "USERNEW", 1)).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("createDate", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    MvcResult result = mockMvc
        .perform(get(STUDENT +"/"+ entitiesFromDB.get(0).getStudentID().toString() + HISTORY+PAGINATED).with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY"))).param("sort", sort)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].historyActivityCode").value("USERNEW"));
  }

  @Test
  public void testReadStudentHistoryPaginated_givenInvalidStudentID_ShouldReturnEmptyContent() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntity(student, "USEREDIT", 2), createStudentHistoryEntity(student, "USERNEW", 1)).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("createDate", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    MvcResult result = mockMvc
        .perform(get(STUDENT +"/"+ UUID.randomUUID().toString() + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("sort", sort)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  private StudentHistoryEntity createStudentHistoryEntity(StudentEntity student, String historyActivityCode, int yearsBefore) {
    var studentHistory = new StudentHistoryEntity();
    BeanUtils.copyProperties(student, studentHistory);
    studentHistory.setHistoryActivityCode(historyActivityCode);
    studentHistory.setCreateDate(LocalDateTime.now().minusYears(yearsBefore));
    return studentHistory;
  }

  private StudentHistoryEntity createStudentHistoryEntityWithLegalNameChange(StudentEntity student, String historyActivityCode, int yearsBefore,
                                                                             String legalFirstName, String legalLastName, String legalMiddleNames) {
    var studentHistory = new StudentHistoryEntity();
    BeanUtils.copyProperties(student, studentHistory);
    studentHistory.setLegalFirstName(legalFirstName);
    studentHistory.setLegalLastName(legalLastName);
    studentHistory.setLegalMiddleNames(legalMiddleNames);
    studentHistory.setHistoryActivityCode(historyActivityCode);
    studentHistory.setCreateDate(LocalDateTime.now().minusYears(yearsBefore));
    return studentHistory;
  }

  @Test
  public void testGetStudentHistoryActivityCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get(STUDENT+HISTORY_ACTIVITY_CODES).with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_CODES")))).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].historyActivityCode").value("USEREDIT"));
  }

  @Test
  public void testReadStudentHistoryPaginated_ShouldReturnStatusOkAndEmptyRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntity(student, "USEREDIT", 2), createStudentHistoryEntity(student, "USERNEW", 1)).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalFirstName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    SearchCriteria criteria = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.EQUAL).value("NO_NAME").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("sort", sort)
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  public void testReadStudentHistoryPaginated_legalFirstName_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName() + "TEST2", student.getLegalLastName(), student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName() + "TEST1", student.getLegalLastName(), student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalFirstName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    SearchCriteria criteria = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.EQUAL).value(entitiesFromDB.get(0).getLegalFirstName() + "TEST2").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("pageNumber", "0").param("pageSize", "5")
            .param("sort", sort)
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].historyActivityCode").value("USEREDIT"));
  }

  @Test
  public void testReadStudentHistoryPaginated_legalLastName_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName(), student.getLegalLastName() + "TEST2", student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName(), student.getLegalLastName() + "TEST1", student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.EQUAL).value(entitiesFromDB.get(0).getLegalLastName() + "TEST2").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("pageNumber", "0").param("pageSize", "5")
            .param("sort", sort)
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].historyActivityCode").value("USEREDIT"));
  }

  @Test
  public void testReadStudentHistoryPaginated_legalLastNameStartWith_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName(), student.getLegalLastName() + "TEST2", student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName(), student.getLegalLastName() + "TEST1", student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.STARTS_WITH).value(entitiesFromDB.get(0).getLegalLastName()).valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("pageNumber", "0").param("pageSize", "5")
            .param("sort", sort)
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].historyActivityCode").value("USEREDIT"));
  }

  @Test
  public void testReadStudentHistoryPaginated_legalLastNameEndWith_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName(), student.getLegalLastName() + "TEST2", student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName(), student.getLegalLastName() + "TEST1", student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.ENDS_WITH).value("TEST2").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("pageNumber", "0").param("pageSize", "10")
            .param("sort", sort)
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(6)));
  }

  @Test
  public void testReadStudentHistoryPaginated_legalFirstNameOrLegalLastNameAndDOB_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName(), student.getLegalLastName() + "TEST2", student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName(), student.getLegalLastName() + "TEST1", student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);


    SearchCriteria lastNameCriteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.EQUAL).value(entitiesFromDB.get(0).getLegalLastName() + "TEST2").valueType(ValueType.STRING).build();
    SearchCriteria firstNameCriteria = SearchCriteria.builder().condition(OR).key("legalFirstName").operation(FilterOperation.CONTAINS).value(entitiesFromDB.get(0).getLegalFirstName()).valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList1 = new ArrayList<>();
    criteriaList1.add(lastNameCriteria);
    criteriaList1.add(firstNameCriteria);

    String fromDate = "2017-12-01";
    String toDate = "2018-06-01";
    SearchCriteria dobCriteria = SearchCriteria.builder().key("dob").operation(FilterOperation.BETWEEN).value(fromDate + "," + toDate).valueType(ValueType.DATE).build();
    List<SearchCriteria> criteriaList2 = new ArrayList<>();
    criteriaList2.add(dobCriteria);

    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList1).build());
    searches.add(Search.builder().condition(AND).searchCriteriaList(criteriaList2).build());

    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("pageNumber", "0").param("pageSize", "5")
            .param("sort", sort)
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].historyActivityCode").value("USEREDIT"))
        .andExpect(jsonPath("$.content[1].historyActivityCode").value("USERNEW"));
  }

  @Test
  public void testReadStudentHistoryPaginated_givenSubmitDataBetween_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName(), student.getLegalLastName() + "TEST2", student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName(), student.getLegalLastName() + "TEST1", student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);

    String fromDate = "2017-12-01";
    String toDate = "2018-06-01";
    SearchCriteria criteria1 = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.EQUAL).value(entitiesFromDB.get(0).getLegalLastName() + "TEST2").valueType(ValueType.STRING).build();
    SearchCriteria criteria2 = SearchCriteria.builder().condition(AND).key("dob").operation(FilterOperation.BETWEEN).value(fromDate + "," + toDate).valueType(ValueType.DATE).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria1);
    criteriaList.add(criteria2);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("pageNumber", "0").param("pageSize", "5")
            .param("sort", sort)
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].historyActivityCode").value("USEREDIT"));
  }

  @Test
  public void testReadStudentHistoryPaginated_legalLastNameOrlegalFirstNameOrGivenUUID_ShouldReturnStatusBadRequest() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName(), student.getLegalLastName() + "TEST2", student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName(), student.getLegalLastName() + "TEST1", student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    SearchCriteria lastNameCriteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.EQUAL).value(entitiesFromDB.get(0).getLegalLastName() + "TEST2").valueType(ValueType.STRING).build();
    SearchCriteria firstNameCriteria = SearchCriteria.builder().condition(OR).key("legalFirstName").operation(FilterOperation.CONTAINS).value(entitiesFromDB.get(0).getLegalFirstName()).valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList1 = new ArrayList<>();
    criteriaList1.add(lastNameCriteria);
    criteriaList1.add(firstNameCriteria);

    SearchCriteria uuidCriteria = SearchCriteria.builder().key("studentHistoryID").operation(FilterOperation.EQUAL).value(studentHistoryEntities.get(0).getStudentHistoryID().toString()).valueType(ValueType.UUID).build();
    List<SearchCriteria> criteriaList2 = new ArrayList<>();
    criteriaList2.add(uuidCriteria);

    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList1).build());
    searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaList2).build());

    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    MvcResult result = mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("pageNumber", "0").param("pageSize", "5")
            .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();

    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  public void testReadStudentHistoryPaginated_givenOperationTypeNull_ShouldReturnStatusBadRequest() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
        List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
            student.getLegalFirstName(), student.getLegalLastName() + "TEST2", student.getLegalMiddleNames()),
            createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                student.getLegalFirstName(), student.getLegalLastName() + "TEST1", student.getLegalMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    SearchCriteria criteria = SearchCriteria.builder().key("studentID").operation(null).value(entitiesFromDB.get(0).getStudentID().toString()).valueType(ValueType.UUID).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);

    this.mockMvc.perform(get(STUDENT + HISTORY+PAGINATED)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
        .param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testReadStudentHistoryPaginated_givenInvalidSearchCriteria_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc
        .perform(get(STUDENT + HISTORY+PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_STUDENT_HISTORY")))
            .param("searchCriteriaList", "{test}")
            .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private StudentHistoryActivityCodeEntity createStudentHistoryActivityCodeData() {
    return StudentHistoryActivityCodeEntity.builder().historyActivityCode("USEREDIT").description("USEREDIT")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }
}
