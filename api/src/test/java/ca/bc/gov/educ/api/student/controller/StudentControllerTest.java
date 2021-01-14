package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.controller.v1.StudentController;
import ca.bc.gov.educ.api.student.filter.FilterOperation;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.*;
import ca.bc.gov.educ.api.student.repository.v1.*;
import ca.bc.gov.educ.api.student.service.v1.StudentService;
import ca.bc.gov.educ.api.student.struct.v1.*;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import ca.bc.gov.educ.api.student.validator.StudentPayloadValidator;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.student.constant.v1.URL.*;
import static ca.bc.gov.educ.api.student.struct.v1.Condition.AND;
import static ca.bc.gov.educ.api.student.struct.v1.Condition.OR;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = StudentApiApplication.class)
@AutoConfigureMockMvc
public class StudentControllerTest {

  private static final StudentMapper mapper = StudentMapper.mapper;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  StudentController controller;

  @Autowired
  StudentRepository repository;

  @Autowired
  GenderCodeTableRepository genderRepo;

  @Autowired
  SexCodeTableRepository sexRepo;

  @Autowired
  DemogCodeTableRepository demogRepo;

  @Autowired
  StatusCodeTableRepository statusRepo;

  @Autowired
  GradeCodeTableRepository gradeRepo;

  @Autowired
  StudentPayloadValidator validator;

  @Autowired
  StudentService studentService;
  @Autowired
  StudentHistoryActivityCodeTableRepository studentHistoryActivityCodeTableRepo;
  @Autowired
  StudentHistoryRepository studentHistoryRepo;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    genderRepo.save(createGenderCodeData());
    sexRepo.save(createSexCodeData());
    demogRepo.save(createDemogCodeData());
    statusRepo.save(createStatusCodeData());
    gradeRepo.save(createGradeCodeData());
    studentHistoryActivityCodeTableRepo.save(createStudentHistoryActivityCodeData());
  }

  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    genderRepo.deleteAll();
    sexRepo.deleteAll();
    demogRepo.deleteAll();
    statusRepo.deleteAll();
    gradeRepo.deleteAll();
    studentHistoryRepo.deleteAll();
    repository.deleteAll();
  }

  private SexCodeEntity createSexCodeData() {
    return SexCodeEntity.builder().sexCode("M").description("Male")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private GenderCodeEntity createGenderCodeData() {
    return GenderCodeEntity.builder().genderCode("M").description("Male")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private StatusCodeEntity createStatusCodeData() {
    return StatusCodeEntity.builder().statusCode("A").description("Active")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private DemogCodeEntity createDemogCodeData() {
    return DemogCodeEntity.builder().demogCode("A").description("Accepted")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private GradeCodeEntity createGradeCodeData() {
    return GradeCodeEntity.builder().gradeCode("01").description("First Grade")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }


  @Test
  public void testRetrieveStudent_GivenRandomID_ShouldThrowEntityNotFoundException() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    this.mockMvc.perform(get(STUDENT + UUID.randomUUID()).with(mockAuthority)).andDo(print()).andExpect(status().isNotFound());
  }


  @Test
  public void testRetrieveStudent_GivenValidID_ShouldReturnStatusOK() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    StudentEntity entity = repository.save(createStudent());
    this.mockMvc.perform(get(STUDENT + "/"+entity.getStudentID()).with(mockAuthority)).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.studentID").value(entity.getStudentID().toString()));
  }


  @Test
  public void testRetrieveStudent_GivenPEN_ShouldReturnStatusOK() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    StudentEntity entity = repository.save(createStudent());
    this.mockMvc.perform(get(STUDENT + "/?pen=" + entity.getPen()).with(mockAuthority)).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].studentID").value(entity.getStudentID().toString()));
  }

  @Test
  public void testCreateStudent_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    var student = getStudentCreate();
    this.mockMvc.perform(post(STUDENT)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(asJsonString(student))
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT"))))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.legalFirstName").value(student.getLegalFirstName().toUpperCase()));
  }


  @Test
  public void testCreateStudent_GivenInvalidPayload_ShouldReturnStatusBadRequest() throws Exception {
    var student = getStudentCreate();
    student.setSexCode("J");
    this.mockMvc.perform(post(STUDENT).with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT"))).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(student))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateStudent_GivenMalformedPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(STUDENT).with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT"))).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content("{test}")).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateStudent_GivenInvalidEmailVerifiedAttribute_ShouldReturnStatusBadRequest() throws Exception {
    var student = getStudentCreate();
    student.setEmailVerified("WRONG");
    this.mockMvc.perform(post(STUDENT).with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT"))).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(student))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateStudent_GivenInvalidHistoryActivityCodeAttribute_ShouldReturnStatusBadRequest() throws Exception {
    var student = getStudentCreate();
    student.setHistoryActivityCode("WRONG");
    this.mockMvc.perform(post(STUDENT).with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT"))).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(student))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdateStudent_GivenValidPayload_ShouldReturnStatusOk() throws Exception {
    StudentEntity entity = createStudent();
    repository.save(entity);
    entity.setLegalFirstName("updated");
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("USEREDIT");
    BeanUtils.copyProperties(StudentMapper.mapper.toStructure(entity), studentUpdate);
    this.mockMvc.perform(put(STUDENT+"/{id}", entity.getStudentID().toString()).with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT"))).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(studentUpdate))).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.legalFirstName").value(entity.getLegalFirstName().toUpperCase()));
  }

  @Test
  public void testUpdateStudent_GivenInvalidHistoryActivityCode_ShouldReturnStatusBadRequest() throws Exception {
    StudentEntity entity = createStudent();
    repository.save(entity);
    entity.setLegalFirstName("updated");
    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(entity.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("WRONG");
    BeanUtils.copyProperties(StudentMapper.mapper.toStructure(entity), studentUpdate);
    this.mockMvc.perform(put(STUDENT+"/{id}", entity.getStudentID().toString()).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(studentUpdate)).with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_STUDENT")))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testDeleteStudent_GivenValidId_ShouldReturnStatus204() throws Exception {
    StudentEntity entity = createStudent();
    repository.save(entity);
    this.mockMvc.perform(delete(STUDENT +"/"+ entity.getStudentID().toString()).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_STUDENT")))).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  public void testDeleteStudent_GivenInvalidId_ShouldReturnStatus404() throws Exception {
    this.mockMvc.perform(delete(STUDENT + UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_STUDENT")))).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReadStudentPaginated_Always_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT+PAGINATED+"?pageSize=2").with(mockAuthority)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  public void testReadStudentPaginated_whenNoDataInDB_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  public void testReadStudentPaginatedWithSorting_Always_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "ASC");
    sortMap.put("legalFirstName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("pageNumber", "1").param("pageSize", "5").param("sort", sort)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadStudentPaginated_GivenFirstNameFilter_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    SearchCriteria criteria = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.EQUAL).value("Leonor").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadStudentPaginated_GivenLastNameFilter_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.EQUAL).value("Warner").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadStudentPaginated_GivenSubmitDateBetween_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    String fromDate = "2017-04-01";
    String toDate = "2018-04-15";
    SearchCriteria criteria = SearchCriteria.builder().key("dob").operation(FilterOperation.BETWEEN).value(fromDate + "," + toDate).valueType(ValueType.DATE).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadStudentPaginated_GivenFirstAndLast_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    String fromDate = "1990-04-01";
    String toDate = "2020-04-15";
    SearchCriteria criteria = SearchCriteria.builder().key("dob").operation(FilterOperation.BETWEEN).value(fromDate + "," + toDate).valueType(ValueType.DATE).build();
    SearchCriteria criteriaFirstName = SearchCriteria.builder().condition(AND).key("legalFirstName").operation(FilterOperation.CONTAINS).value("a").valueType(ValueType.STRING).build();
    SearchCriteria criteriaLastName = SearchCriteria.builder().condition(AND).key("legalLastName").operation(FilterOperation.CONTAINS).value("l").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    criteriaList.add(criteriaFirstName);
    criteriaList.add(criteriaLastName);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(3)));
  }

  @Test
  public void testReadStudentPaginated_GivenFirstAndLastOrDOB_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
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
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(6)));
  }

  @Test
  public void testReadStudentPaginated_GivenFirstORLastANDDOB_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });

    SearchCriteria criteriaFirstName = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.CONTAINS).value("a").valueType(ValueType.STRING).build();
    SearchCriteria criteriaLastName = SearchCriteria.builder().condition(OR).key("legalLastName").operation(FilterOperation.CONTAINS).value("l").valueType(ValueType.STRING).build();
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
    searches.add(Search.builder().condition(AND).searchCriteriaList(criteriaList1).build());

    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(5)));
  }

  @Test
  public void testReadStudentPaginated_LegalLastNameFilterIgnoreCase_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.CONTAINS).value("b").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  public void testReadStudentPaginated_LegalLastNameStartWith_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.STARTS_WITH).value("Ham").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadStudentPaginated_LegalLastNameStartWith2_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.STARTS_WITH).value("hem").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  public void testReadStudentPaginated_LegalLastNameStartWithIgnoreCase2_ShouldReturnStatusOk() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });

    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    val entitiesFromDB = repository.findAll();
    SearchCriteria criteria = SearchCriteria.builder().key("studentID").operation(FilterOperation.EQUAL).value(entitiesFromDB.get(0).getStudentID().toString()).valueType(ValueType.UUID).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadStudentPaginated_LegalLastNameEndWith_ShouldReturnStatusOkAndRecord() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.ENDS_WITH).value("ton").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  public void testReadStudentPaginated_LegalLastNameEndWith_ShouldReturnStatusOkButNoRecord() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.ENDS_WITH).value("son").valueType(ValueType.STRING).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    System.out.println(criteriaJSON);
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  public void testReadStudentPaginated_givenOperationTypeNull_ShouldReturnStatusBadRequest() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });

    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));
    val entitiesFromDB = repository.findAll();
    SearchCriteria criteria = SearchCriteria.builder().key("studentID").operation(null).value(entitiesFromDB.get(0).getStudentID().toString()).valueType(ValueType.UUID).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    this.mockMvc.perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testReadStudentPaginated_givenInvalidSearchCriteria_ShouldReturnStatusBadRequest() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    this.mockMvc
        .perform(get(STUDENT + PAGINATED).with(mockAuthority).param("searchCriteriaList", "{test}")
            .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGetGenderCodes_ShouldReturnCodes() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT_CODES";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    this.mockMvc.perform(get(STUDENT + GENDER_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].genderCode").value("M"));
  }

  @Test
  public void testGetSexCodes_ShouldReturnCodes() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT_CODES";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    this.mockMvc.perform(get(STUDENT + SEX_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sexCode").value("M"));
  }

  @Test
  public void testGetDemogCodes_ShouldReturnCodes() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT_CODES";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    this.mockMvc.perform(get(STUDENT + DEMOG_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].demogCode").value("A"));
  }

  @Test
  public void testGetGradeCodes_ShouldReturnCodes() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT_CODES";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    this.mockMvc.perform(get(STUDENT + GRADE_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gradeCode").value("01"));
  }

  @Test
  public void testGetStatusCodes_ShouldReturnCodes() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_STUDENT_CODES";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    this.mockMvc.perform(get(STUDENT + STATUS_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].statusCode").value("A"));
  }

  private StudentEntity createStudent() {
    StudentEntity student = new StudentEntity();
    student.setPen("987654321");
    student.setLegalFirstName("John");
    student.setLegalMiddleNames("Duke");
    student.setLegalLastName("Wayne");
    student.setDob(LocalDate.parse("1907-05-26"));
    student.setSexCode("M");
    student.setUsualFirstName("Johnny");
    student.setUsualMiddleNames("Duke");
    student.setUsualLastName("Wayne");
    student.setEmail("theduke@someplace.com");
    student.setEmailVerified("Y");
    student.setDeceasedDate(LocalDate.parse("1979-06-11"));
    return student;
  }

  private StudentCreate getStudentCreate(Optional<String> pen) {
    var studentEntity = createStudent();
    var studentCreate = new StudentCreate();
    BeanUtils.copyProperties(mapper.toStructure(studentEntity), studentCreate);
    studentCreate.setHistoryActivityCode("USEREDIT");
    pen.ifPresent(studentCreate::setPen);
    return studentCreate;
  }

  private StudentCreate getStudentCreate() {
    return getStudentCreate(Optional.empty());
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
