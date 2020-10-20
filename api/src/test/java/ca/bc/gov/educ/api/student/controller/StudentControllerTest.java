package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.student.filter.FilterOperation;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.*;
import ca.bc.gov.educ.api.student.support.WithMockOAuth2Scope;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.student.struct.Condition.AND;
import static ca.bc.gov.educ.api.student.struct.Condition.OR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = StudentApiApplication.class)
public class StudentControllerTest {

  private static final StudentMapper mapper = StudentMapper.mapper;
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
  StudentMergeRepository studentMergeRepository;

  @Autowired
  StudentTwinRepository studentTwinRepository;

  @Autowired
  StudentPayloadValidator validator;

  @Autowired
  StudentService studentService;
  @Autowired
  StudentMergeDirectionCodeTableRepository mergeDirectionCodeRepo;

  @Autowired
  StudentMergeSourceCodeTableRepository mergeSourceCodeRepo;
  @Autowired
  StudentTwinReasonCodeTableRepository twinReasonCodeRepo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new RestExceptionHandler()).build();
    genderRepo.save(createGenderCodeData());
    sexRepo.save(createSexCodeData());
    demogRepo.save(createDemogCodeData());
    statusRepo.save(createStatusCodeData());
    gradeRepo.save(createGradeCodeData());
    mergeDirectionCodeRepo.save(createStudentMergeDirectionCodeData());
    mergeSourceCodeRepo.save(createStudentMergeSourceCodeData());
    twinReasonCodeRepo.save(createStudentTwinReasonCodeData());
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
    repository.deleteAll();
    studentMergeRepository.deleteAll();
    studentTwinRepository.deleteAll();
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
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testRetrieveStudent_GivenRandomID_ShouldThrowEntityNotFoundException() throws Exception {
    this.mockMvc.perform(get("/" + UUID.randomUUID())).andDo(print()).andExpect(status().isNotFound());
  }


  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testRetrieveStudent_GivenValidID_ShouldReturnStatusOK() throws Exception {
    StudentEntity entity = repository.save(createStudent());
    this.mockMvc.perform(get("/" + entity.getStudentID())).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.studentID").value(entity.getStudentID().toString()));
  }


  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testRetrieveStudent_GivenPEN_ShouldReturnStatusOK() throws Exception {
    StudentEntity entity = repository.save(createStudent());
    this.mockMvc.perform(get("/?pen=" + entity.getPen())).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$[0].studentID").value(entity.getStudentID().toString()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudent_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    StudentEntity entity = createStudent();
    this.mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMapper.mapper.toStructure(entity)))).andDo(print()).andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.legalFirstName").value(entity.getLegalFirstName().toUpperCase()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  @Transactional
  public void testCreateStudent_GivenValidPayloadWithTwinsAssociations_ShouldReturnStatusCreated() throws Exception {
    Student student = StudentMapper.mapper.toStructure(createStudent());
    StudentEntity studentTwin = createStudent();
    studentTwin.setPen("120164444");
    studentService.createStudent(studentTwin);
    List<StudentTwinAssociation> studentTwinAssociations = new ArrayList<>();
    StudentTwinAssociation studentTwinAssociation = new StudentTwinAssociation();
    studentTwinAssociation.setStudentTwinReasonCode("PENMATCH");
    studentTwinAssociation.setTwinStudentID(studentTwin.getStudentID().toString());
    studentTwinAssociations.add(studentTwinAssociation);
    student.setStudentTwinAssociations(studentTwinAssociations);

    this.mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(student))).andDo(print()).andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.legalFirstName").value(student.getLegalFirstName().toUpperCase()));

    var studentFromDB = studentService.retrieveStudentByPen(student.getPen());
    assertThat(studentFromDB).isPresent();
    var twinRecords = studentTwinRepository.findStudentTwinEntityByStudentIDOrTwinStudent_StudentID(studentFromDB.get().getStudentID(),studentFromDB.get().getStudentID());
    assertThat(twinRecords).isNotEmpty().size().isEqualTo(1);
    var twinRecordsFromOtherSide = studentTwinRepository.findStudentTwinEntityByStudentIDOrTwinStudent_StudentID(studentTwin.getStudentID(), studentTwin.getStudentID());
    assertThat(twinRecordsFromOtherSide).isNotEmpty().size().isEqualTo(1);

  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  @Transactional
  public void testCreateStudent_GivenValidPayloadWithMergeAssociations_ShouldReturnStatusCreated() throws Exception {
    Student student = StudentMapper.mapper.toStructure(createStudent());
    StudentEntity studentMerge = createStudent();
    studentMerge.setPen("120164444");
    studentService.createStudent(studentMerge);
    List<StudentMergeAssociation> studentMergeAssociations = new ArrayList<>();
    StudentMergeAssociation studentMergeAssociation = new StudentMergeAssociation();
    studentMergeAssociation.setStudentMergeDirectionCode("FROM");
    studentMergeAssociation.setStudentMergeSourceCode("MINISTRY");
    studentMergeAssociation.setMergeStudentID(studentMerge.getStudentID().toString());
    studentMergeAssociations.add(studentMergeAssociation);
    student.setStudentMergeAssociations(studentMergeAssociations);

    this.mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(student))).andDo(print()).andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.legalFirstName").value(student.getLegalFirstName().toUpperCase()));

    var studentFromDB = studentService.retrieveStudentByPen(student.getPen());
    assertThat(studentFromDB).isPresent();
    var mergeRecords = studentMergeRepository.findStudentMergeEntityByStudentID(studentFromDB.get().getStudentID());
    assertThat(mergeRecords).isNotEmpty().size().isEqualTo(1);
    var twinRecordsFromOtherSide = studentMergeRepository.findStudentMergeEntityByMergeStudent(studentMerge);
    assertThat(twinRecordsFromOtherSide).isNotEmpty().size().isEqualTo(1);

  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudent_GivenInvalidPayload_ShouldReturnStatusBadRequest() throws Exception {
    StudentEntity entity = createStudent();
    entity.setSexCode("J");
    this.mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMapper.mapper.toStructure(entity)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudent_GivenMalformedPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content("{test}")).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudent_GivenInvalidEmailVerifiedAttribute_ShouldReturnStatusBadRequest() throws Exception {
    StudentEntity entity = createStudent();
    entity.setEmailVerified("WRONG");
    this.mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMapper.mapper.toStructure(entity)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testUpdateStudent_GivenValidPayload_ShouldReturnStatusOk() throws Exception {
    StudentEntity entity = createStudent();
    repository.save(entity);
    entity.setLegalFirstName("updated");
    this.mockMvc.perform(put("/").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMapper.mapper.toStructure(entity)))).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.legalFirstName").value(entity.getLegalFirstName().toUpperCase()));;
  }

  @Test
  @WithMockOAuth2Scope(scope = "DELETE_STUDENT")
  public void testDeleteStudent_GivenValidId_ShouldReturnStatus204() throws Exception {
    StudentEntity entity = createStudent();
    repository.save(entity);
    StudentEntity mergedFromStudent = repository.save(createStudent());

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(entity.getStudentID());
    studentMergeFrom.setMergeStudent(mergedFromStudent);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    studentMergeFrom.setUpdateUser("Test User");
    studentMergeRepository.save(studentMergeFrom);
    StudentTwinEntity penmatchTwin = new StudentTwinEntity();
    penmatchTwin.setStudentID(entity.getStudentID());
    penmatchTwin.setTwinStudent(mergedFromStudent);
    penmatchTwin.setStudentTwinReasonCode("PENMATCH");
    studentTwinRepository.save(penmatchTwin);


    this.mockMvc.perform(delete("/" + entity.getStudentID().toString()).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  @WithMockOAuth2Scope(scope = "DELETE_STUDENT")
  public void testDeleteStudent_GivenInvalidId_ShouldReturnStatus404() throws Exception {
    this.mockMvc.perform(delete("/" + UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_Always_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated?pageSize=2")
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_whenNoDataInDB_ShouldReturnStatusOk() throws Exception {
    MvcResult result = mockMvc
        .perform(get("/paginated")
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginatedWithSorting_Always_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "ASC");
    sortMap.put("legalFirstName", "DESC");
    String sort = new ObjectMapper().writeValueAsString(sortMap);
    MvcResult result = mockMvc
        .perform(get("/paginated").param("pageNumber", "1").param("pageSize", "5").param("sort", sort)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_GivenFirstNameFilter_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_GivenLastNameFilter_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_GivenSubmitDateBetween_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_GivenFirstAndLast_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(3)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_GivenFirstAndLastOrDOB_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(6)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_GivenFirstORLastANDDOB_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(5)));
  }
  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_LegalLastNameFilterIgnoreCase_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_LegalLastNameStartWith_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_LegalLastNameStartWith2_ShouldReturnStatusOk() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_LegalLastNameStartWithIgnoreCase2_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });

    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    val entitiesFromDB = repository.findAll();
    SearchCriteria criteria = SearchCriteria.builder().key("studentID").operation(FilterOperation.EQUAL).value(entitiesFromDB.get(0).getStudentID().toString()).valueType(ValueType.UUID).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    MvcResult result = mockMvc
        .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_LegalLastNameEndWith_ShouldReturnStatusOkAndRecord() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
      .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON))
      .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_LegalLastNameEndWith_ShouldReturnStatusOkButNoRecord() throws Exception {
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
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    MvcResult result = mockMvc
      .perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON))
      .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_givenOperationTypeNull_ShouldReturnStatusBadRequest() throws Exception {
    var file = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });

    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    val entitiesFromDB = repository.findAll();
    SearchCriteria criteria = SearchCriteria.builder().key("studentID").operation(null).value(entitiesFromDB.get(0).getStudentID().toString()).valueType(ValueType.UUID).build();
    List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    ObjectMapper objectMapper = new ObjectMapper();
    String criteriaJSON = objectMapper.writeValueAsString(searches);
    this.mockMvc.perform(get("/paginated").param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testReadStudentPaginated_givenInvalidSearchCriterial_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc
        .perform(get("/paginated").param("searchCriteriaList", "{test}")
            .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetGenderCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/gender-codes")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].genderCode").value("M"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetSexCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/sex-codes")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sexCode").value("M"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetDemogCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/demog-codes")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].demogCode").value("A"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetGradeCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/grade-codes")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gradeCode").value("01"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetStatusCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/status-codes")).andDo(print()).andExpect(status().isOk())
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

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  private StudentTwinReasonCodeEntity createStudentTwinReasonCodeData() {
    return StudentTwinReasonCodeEntity.builder().twinReasonCode("PENMATCH").description("PENMATCH")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }
  private StudentMergeDirectionCodeEntity createStudentMergeDirectionCodeData() {
    return StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("FROM").description("Merge From")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }

  private StudentMergeSourceCodeEntity createStudentMergeSourceCodeData() {
    return StudentMergeSourceCodeEntity.builder().mergeSourceCode("MINISTRY").description("MINISTRY")
        .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
  }
}
