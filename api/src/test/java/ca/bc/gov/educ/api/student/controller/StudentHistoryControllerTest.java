package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.struct.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    MockitoAnnotations.initMocks(this);
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

  private StudentHistoryEntity createStudentHistoryEntity(StudentEntity student, String historyActivityCode, int yeasBefore) {
    var studentHistory = new StudentHistoryEntity();
    BeanUtils.copyProperties(student, studentHistory);
    studentHistory.setHistoryActivityCode(historyActivityCode);
    studentHistory.setCreateDate(LocalDateTime.now().minusYears(yeasBefore));
    return studentHistory;
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetStudentHistoryActivityCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/student-history-activity-codes")).andDo(print()).andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].historyActivityCode").value("USEREDIT"));
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
