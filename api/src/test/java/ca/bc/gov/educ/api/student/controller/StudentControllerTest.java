package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.StudentApiApplication;
import ca.bc.gov.educ.api.student.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.GenderCodeEntity;
import ca.bc.gov.educ.api.student.model.SexCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.repository.GenderCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.SexCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.support.WithMockOAuth2Scope;
import ca.bc.gov.educ.api.student.validator.StudentPayloadValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = StudentApiApplication.class)
public class StudentControllerTest {
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
  StudentPayloadValidator validator;
  
  @Autowired
  StudentService studentService;


  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestExceptionHandler()).build();
    genderRepo.save(createGenderCodeData());
    sexRepo.save(createSexCodeData());
  }
  
  /**
   * need to delete the records to make it working in unit tests assertion, else the records will keep growing and assertions will fail.
   */
  @After
  public void after() {
    genderRepo.deleteAll();
    sexRepo.deleteAll();
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
            .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMapper.mapper.toStructure(entity)))).andDo(print()).andExpect(status().isCreated());
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
  public void testUpdateStudent_GivenValidPayload_ShouldReturnStatusOk() throws Exception {
    StudentEntity entity = createStudent();
    repository.save(entity);
    entity.setLegalFirstName("updated");
    this.mockMvc.perform(put("/").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(asJsonString(StudentMapper.mapper.toStructure(entity)))).andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.legalFirstName").value(entity.getLegalFirstName()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "DELETE_STUDENT")
  public void testDeleteStudent_GivenValidId_ShouldReturnStatus204() throws Exception {
    StudentEntity entity = createStudent();
    repository.save(entity);
    this.mockMvc.perform(delete("/"+entity.getStudentID().toString()).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  @WithMockOAuth2Scope(scope = "DELETE_STUDENT")
  public void testDeleteStudent_GivenInvalidId_ShouldReturnStatus404() throws Exception {
    this.mockMvc.perform(delete("/"+UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
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
