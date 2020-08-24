package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.student.mappers.StudentTwinMapper;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
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
public class StudentTwinControllerTest {
  private MockMvc mockMvc;
  @Autowired
  StudentTwinController controller;

  @Autowired
  StudentRepository studentRepo;

  @Autowired
  StudentTwinRepository studentTwinRepo;

  @Autowired
  StudentTwinReasonCodeTableRepository twinReasonCodeRepo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestExceptionHandler()).build();
    twinReasonCodeRepo.save(createStudentTwinReasonCodeData());
  }
  
  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    studentTwinRepo.deleteAll();
    studentRepo.deleteAll();
    twinReasonCodeRepo.deleteAll();
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT")
  public void testFindStudentTwins_GivenValidStudentID_ShouldReturnTwinedStudentIDs() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity penmatchTwinendStudent = studentRepo.save(createStudent());
    StudentEntity pencreateTwinedStudent = studentRepo.save(createStudent());

    StudentTwinEntity penmatchTwin = new StudentTwinEntity();
    penmatchTwin.setStudentID(student.getStudentID());
    penmatchTwin.setTwinStudentID(penmatchTwinendStudent.getStudentID());
    penmatchTwin.setStudentTwinReasonCode("PENMATCH");
    studentTwinRepo.save(penmatchTwin);

    StudentTwinEntity pencreateTwin = new StudentTwinEntity();
    pencreateTwin.setStudentID(student.getStudentID());
    pencreateTwin.setTwinStudentID(pencreateTwinedStudent.getStudentID());
    pencreateTwin.setStudentTwinReasonCode("PENCREATE");
    studentTwinRepo.save(pencreateTwin);

    this.mockMvc.perform(get("/" + student.getStudentID() + "/twins")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentTwinReasonCode=='PENMATCH')].twinStudentID").value(penmatchTwinendStudent.getStudentID().toString()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentTwinReasonCode=='PENMATCH')].twinStudent.pen").value(penmatchTwinendStudent.getPen()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.studentTwinReasonCode=='PENCREATE')].twinStudentID").value(pencreateTwinedStudent.getStudentID().toString()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudentTwin_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity penmatchTwinendStudent = studentRepo.save(createStudent());

    StudentTwinEntity penmatchTwin = new StudentTwinEntity();
    penmatchTwin.setStudentID(student.getStudentID());
    penmatchTwin.setTwinStudentID(penmatchTwinendStudent.getStudentID());
    penmatchTwin.setStudentTwinReasonCode("PENMATCH");
    penmatchTwin.setUpdateUser("Test User");

    this.mockMvc.perform(post("/" + student.getStudentID() + "/twins").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
      .content(asJsonString(StudentTwinMapper.mapper.toStructure(penmatchTwin)))).andDo(print()).andExpect(status().isCreated())
      .andExpect(MockMvcResultMatchers.jsonPath("$.twinStudentID").value(penmatchTwin.getTwinStudentID().toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.updateUser").value("Test User"));
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudentTwin_GivenInvalidStudentID_ShouldReturnStatusBadRequest() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity penmatchTwinendStudent = studentRepo.save(createStudent());

    StudentTwinEntity penmatchTwin = new StudentTwinEntity();
    penmatchTwin.setStudentID(student.getStudentID());
    penmatchTwin.setTwinStudentID(penmatchTwinendStudent.getStudentID());
    penmatchTwin.setStudentTwinReasonCode("PENMATCH");

    this.mockMvc.perform(post("/" + penmatchTwinendStudent.getStudentID() + "/twins").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentTwinMapper.mapper.toStructure(penmatchTwin)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "WRITE_STUDENT")
  public void testCreateStudentTwin_GivenInvalidReasonCode_ShouldReturnStatusBadRequest() throws Exception {
    StudentEntity student = studentRepo.save(createStudent());
    StudentEntity penmatchTwinendStudent = studentRepo.save(createStudent());

    StudentTwinEntity penmatchTwin = new StudentTwinEntity();
    penmatchTwin.setStudentID(student.getStudentID());
    penmatchTwin.setTwinStudentID(penmatchTwinendStudent.getStudentID());
    penmatchTwin.setStudentTwinReasonCode("INVALID");

    this.mockMvc.perform(post("/" + student.getStudentID() + "/twins").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentTwinMapper.mapper.toStructure(penmatchTwin)))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_STUDENT_CODES")
  public void testGetStudentTwinReasonCodes_ShouldReturnCodes() throws Exception {
    this.mockMvc.perform(get("/student-twin-reason-codes")).andDo(print()).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].twinReasonCode").value("PENMATCH"));
  }

  private StudentTwinReasonCodeEntity createStudentTwinReasonCodeData() {
    return StudentTwinReasonCodeEntity.builder().twinReasonCode("PENMATCH").description("PENMATCH")
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