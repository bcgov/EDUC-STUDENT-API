package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentHistoryActivityCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.repository.StudentHistoryActivityCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.struct.Student;
import ca.bc.gov.educ.api.student.support.WithMockOAuth2Scope;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = StudentApiApplication.class)
public class StudentHistoryControllerTest {

  private static final StudentMapper mapper = StudentMapper.mapper;
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
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new RestExceptionHandler()).build();
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
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
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
      .perform(get("/" + entitiesFromDB.get(0).getStudentID().toString() + "/student-history/paginated").param("sort", sort)
        .contentType(APPLICATION_JSON))
      .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)))
      .andExpect(jsonPath("$.content[0].historyActivityCode").value("USERNEW"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
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
      .perform(get("/" + UUID.randomUUID().toString() + "/student-history/paginated").param("sort", sort)
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
                                                                             String legalFirstName, String legalLastName, String legalMiddleNames,
                                                                             String usualFirstName, String usualLastName, String usualMiddleNames) {
    var studentHistory = new StudentHistoryEntity();
    BeanUtils.copyProperties(student, studentHistory);
    studentHistory.setLegalFirstName(legalFirstName);
    studentHistory.setLegalLastName(legalLastName);
    studentHistory.setLegalMiddleNames(legalMiddleNames);
    studentHistory.setUsualFirstName(usualFirstName);
    studentHistory.setUsualLastName(usualLastName);
    studentHistory.setUsualMiddleNames(usualMiddleNames);
    studentHistory.setHistoryActivityCode(historyActivityCode);
    studentHistory.setCreateDate(LocalDateTime.now().minusYears(yearsBefore));
    return studentHistory;
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetStudentHistoryActivityCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/student-history-activity-codes")).andDo(print()).andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].historyActivityCode").value("USEREDIT"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
  public void testReadStudentHistoryNameVariant_givenStudentID_ShouldReturnStatusOkAndNoRecord() throws Exception {
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

    this.mockMvc
            .perform(get("/student-history/name-variant")
                    .param("legalFirstName", "NO-NAME")
            ).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.legalFirstNames").isEmpty())
            .andExpect(jsonPath("$.legalLastNames").isEmpty())
            .andExpect(jsonPath("$.legalMiddleNames").isEmpty())
            .andExpect(jsonPath("$.usualFirstNames").isEmpty())
            .andExpect(jsonPath("$.usualLastNames").isEmpty())
            .andExpect(jsonPath("$.usualMiddleNames").isEmpty());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
  public void testReadStudentHistoryNameVariant_ByLegalFirstName_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
            List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
                    student.getLegalFirstName() + "Test2", student.getLegalLastName(), student.getLegalMiddleNames(),"", "", ""),
                    createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                    student.getLegalFirstName() + "Test1", student.getLegalLastName(), student.getLegalMiddleNames(),"", "", "")).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    this.mockMvc
            .perform(get("/student-history/name-variant")
                    .param("legalFirstName", entitiesFromDB.get(0).getLegalFirstName() + "Test2" )
                    .param("legalLastName", entitiesFromDB.get(0).getLegalLastName())
                    .param("legalMiddleNames", entitiesFromDB.get(0).getLegalMiddleNames())
            ).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.legalFirstNames[0]").value(entitiesFromDB.get(0).getLegalFirstName()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
  public void testReadStudentHistoryNameVariant_ByLegalLastName_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
            List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
                    student.getLegalFirstName(), student.getLegalLastName() + "Test2", student.getLegalMiddleNames(),"", "", ""),
                    createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                            student.getLegalFirstName(), student.getLegalLastName() + "Test2", student.getLegalMiddleNames(),"", "", "")).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    this.mockMvc
            .perform(get("/student-history/name-variant")
                    .param("legalFirstName", entitiesFromDB.get(0).getLegalFirstName())
                    .param("legalLastName", entitiesFromDB.get(0).getLegalLastName() + "Test2")
                    .param("legalMiddleNames", entitiesFromDB.get(0).getLegalMiddleNames())
            ).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.legalLastNames[0]").value(entitiesFromDB.get(0).getLegalLastName()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
  public void testReadStudentHistoryNameVariant_ByLegalMiddleNames_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
            List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
                    student.getLegalFirstName(), student.getLegalLastName(), student.getLegalMiddleNames() + "Test2","", "", ""),
                    createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                            student.getLegalFirstName(), student.getLegalLastName(), student.getLegalMiddleNames() + "Test2","", "", "")).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    this.mockMvc
            .perform(get("/student-history/name-variant")
                    .param("legalFirstName", entitiesFromDB.get(0).getLegalFirstName())
                    .param("legalLastName", entitiesFromDB.get(0).getLegalLastName())
                    .param("legalMiddleNames", entitiesFromDB.get(0).getLegalMiddleNames() + "Test2")
            ).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.legalMiddleNames[0]").value(entitiesFromDB.get(0).getLegalMiddleNames()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
  public void testReadStudentHistoryNameVariant_ByUsualFirstName_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
            List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
                    "", "", "", student.getUsualFirstName() + "Test2", student.getUsualLastName(), student.getUsualMiddleNames()),
                    createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                    "", "", "", student.getUsualFirstName() + "Test2", student.getUsualLastName(), student.getUsualMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    this.mockMvc
            .perform(get("/student-history/name-variant")
                    .param("usualFirstName", entitiesFromDB.get(0).getUsualFirstName() + "Test2" )
                    .param("usualLastName", entitiesFromDB.get(0).getUsualLastName())
                    .param("usualMiddleNames", entitiesFromDB.get(0).getUsualMiddleNames())
            ).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.usualFirstNames").value(entitiesFromDB.get(0).getUsualFirstName()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
  public void testReadStudentHistoryNameVariant_ByUsualLastName_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
            List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
                    "", "", "", student.getUsualFirstName(), student.getUsualLastName() + "Test2", student.getUsualMiddleNames()),
                    createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                    "", "", "", student.getUsualFirstName(), student.getUsualLastName() + "Test2", student.getUsualMiddleNames())).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    this.mockMvc
            .perform(get("/student-history/name-variant")
                    .param("usualFirstName", entitiesFromDB.get(0).getUsualFirstName())
                    .param("usualLastName", entitiesFromDB.get(0).getUsualLastName() + "Test2")
                    .param("usualMiddleNames", entitiesFromDB.get(0).getUsualMiddleNames())
            ).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.usualLastNames[0]").value(entitiesFromDB.get(0).getUsualLastName()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_HISTORY")
  public void testReadStudentHistoryNameVariant_ByUsualMiddleNames_ShouldReturnStatusOkAndRecord() throws Exception {
    var file = new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource("mock_students.json")).getFile()
    );
    List<Student> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    repository.saveAll(entities.stream().map(mapper::toModel).map(TransformUtil::uppercaseFields).collect(Collectors.toList()));

    val entitiesFromDB = repository.findAll();
    var studentHistoryEntities = entitiesFromDB.stream().flatMap(student ->
            List.of(createStudentHistoryEntityWithLegalNameChange(student, "USEREDIT", 1,
                    "", "", "", student.getUsualFirstName(), student.getUsualLastName(), student.getUsualMiddleNames() + "Test2"),
                    createStudentHistoryEntityWithLegalNameChange(student, "USERNEW", 2,
                            "", "", "", student.getUsualFirstName(), student.getUsualLastName(), student.getUsualMiddleNames() + "Test2")).stream()
    ).collect(Collectors.toList());
    studentHistoryRepo.saveAll(studentHistoryEntities);

    this.mockMvc
            .perform(get("/student-history/name-variant")
                    .param("usualFirstName", entitiesFromDB.get(0).getUsualFirstName())
                    .param("usualLastName", entitiesFromDB.get(0).getUsualLastName())
                    .param("usualMiddleNames", entitiesFromDB.get(0).getUsualMiddleNames() + "Test2")
            ).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.usualMiddleNames[0]").value(entitiesFromDB.get(0).getUsualMiddleNames()));
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
