package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.student.mappers.StudentMergeMapper;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.struct.StudentMerge;
import ca.bc.gov.educ.api.student.support.WithMockOAuth2Scope;

import org.codehaus.jackson.map.ObjectMapper;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = StudentApiApplication.class)
public class StudentMergeControllerTest {
  private MockMvc mockMvc;
  @Autowired
  StudentMergeController controller;

  @Autowired
  StudentRepository studentRepo;

  @Autowired
  StudentMergeRepository studentMergeRepo;

  @Autowired
  StudentMergeDirectionCodeTableRepository mergeDirectionCodeRepo;

  @Autowired
  StudentMergeSourceCodeTableRepository mergeSourceCodeRepo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestExceptionHandler()).build();
    mergeDirectionCodeRepo.save(createStudentMergeDirectionCodeData());
    mergeSourceCodeRepo.save(createStudentMergeSourceCodeData());
  }
  
  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    studentMergeRepo.deleteAll();
    studentRepo.deleteAll();
    mergeDirectionCodeRepo.deleteAll();
    mergeSourceCodeRepo.deleteAll();
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testFindStudentMerges_GivenValidStudentID_ShouldReturnMergedStudentIDs() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity mergedFromStudent = studentRepo.save(createStudent());
    StudentEntity mergedToStudent = studentRepo.save(createStudent());

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(student.getStudentID());
    studentMergeFrom.setMergeStudent(mergedFromStudent);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    studentMergeRepo.save(studentMergeFrom);

    StudentMergeEntity studentMergeTo = new StudentMergeEntity();
    studentMergeTo.setStudentID(student.getStudentID());
    studentMergeTo.setMergeStudent(mergedToStudent);
    studentMergeTo.setStudentMergeDirectionCode("TO");
    studentMergeTo.setStudentMergeSourceCode("MINISTRY");
    studentMergeRepo.save(studentMergeTo);

    this.mockMvc.perform(get("/" + student.getStudentID() + "/merges")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentMergeDirectionCode=='FROM')].mergeStudentID").value(mergedFromStudent.getStudentID().toString()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentMergeDirectionCode=='FROM')].mergeStudent.pen").value(mergedFromStudent.getPen()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentMergeDirectionCode=='TO')].mergeStudentID").value(mergedToStudent.getStudentID().toString()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudentMerge_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity mergedFromStudent = studentRepo.save(createStudent());

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(student.getStudentID());
    studentMergeFrom.setMergeStudent(mergedFromStudent);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");
    studentMergeFrom.setUpdateUser("Test User");

    StudentMerge studentMergeFromStruct = StudentMergeMapper.mapper.toStructure(studentMergeFrom);

    this.mockMvc.perform(post("/" + student.getStudentID() + "/merges").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
      .content(asJsonString(studentMergeFromStruct))).andDo(print()).andExpect(status().isCreated())
      .andExpect(MockMvcResultMatchers.jsonPath("$.mergeStudentID").value(mergedFromStudent.getStudentID().toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.mergeStudent.pen").value(mergedFromStudent.getPen()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.updateUser").value("Test User"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudentMerge_GivenInvalidStudentID_ShouldReturnStatusBadRequest() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity mergedFromStudent = studentRepo.save(createStudent());

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(student.getStudentID());
    studentMergeFrom.setMergeStudent(mergedFromStudent);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("MINISTRY");

    this.mockMvc.perform(post("/" + mergedFromStudent.getStudentID() + "/merges").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMergeMapper.mapper.toStructure(studentMergeFrom)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudentMerge_GivenInvalidMergeSourceCode_ShouldReturnStatusBadRequest() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity mergedFromStudent = studentRepo.save(createStudent());

    StudentMergeEntity studentMergeFrom = new StudentMergeEntity();
    studentMergeFrom.setStudentID(student.getStudentID());
    studentMergeFrom.setMergeStudent(mergedFromStudent);
    studentMergeFrom.setStudentMergeDirectionCode("FROM");
    studentMergeFrom.setStudentMergeSourceCode("INVALID");

    this.mockMvc.perform(post("/" + student.getStudentID() + "/merges").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMergeMapper.mapper.toStructure(studentMergeFrom)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetStudentMergeSourceCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/student-merge-source-codes")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].mergeSourceCode").value("MINISTRY"));
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
}
