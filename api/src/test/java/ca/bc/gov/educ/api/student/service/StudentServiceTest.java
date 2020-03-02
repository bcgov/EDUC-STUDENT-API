package ca.bc.gov.educ.api.student.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.repository.GenderCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.SexCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.struct.SexCode;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudentServiceTest {

  public static final String PARAMETERS = "parameters";
	
  @Autowired
  StudentRepository repository;
  StudentService service;
  
  @Mock
  ApplicationProperties applicationProperties;

  @Autowired
  GenderCodeTableRepository genderRepo;
  
  @Autowired
  SexCodeTableRepository sexRepo;
  
  @Mock
  RestTemplate template;

  @Before
  public void before() {
    service = new StudentService(repository, genderRepo, sexRepo);
  }

  @Test
  public void testCreateStudent_WhenPayloadIsValid_ShouldReturnSavedObject() {
    StudentEntity student = getStudentEntity();
    assertNotNull(service.createStudent(student));
    assertNotNull(student.getStudentID());
  }

  @Test
  public void testRetrieveStudent_WhenStudentExistInDB_ShouldReturnStudent() {
    StudentEntity student = getStudentEntity();
    assertNotNull(service.createStudent(student));
    assertNotNull(service.retrieveStudent(student.getStudentID()));
  }

  @Test
  public void testRetrieveStudent_WhenStudentDoesNotExistInDB_ShouldThrowEntityNotFoundException() {
    assertThrows(EntityNotFoundException.class, () -> service.retrieveStudent(UUID.fromString("00000000-0000-0000-0000-f3b2d4f20000")));
  }

  @Test
  public void testUpdateStudent_WhenPayloadIsValid_ShouldReturnTheUpdatedObject(){

    StudentEntity student = getStudentEntity();
    student = service.createStudent(student);
    student.setLegalFirstName("updatedFirstName");
    StudentEntity updateEntity = service.updateStudent(student);
    assertNotNull(updateEntity);
    assertThat(updateEntity.getLegalFirstName().equals("updatedFirstName")).isTrue();
  }

  private ResponseEntity<SexCode[]> createSexCodeResponse() {
    return ResponseEntity.ok(createSexCodeArray());
  }

  private SexCode[] createSexCodeArray() {
	SexCode[] sexCodes = new SexCode[2];
    sexCodes[0] = SexCode.builder().sexCode("M").effectiveDate(LocalDateTime.now().toString()).expiryDate(LocalDateTime.MAX.toString()).build();
    sexCodes[1] = SexCode.builder().sexCode("F").effectiveDate(LocalDateTime.now().toString()).expiryDate(LocalDateTime.MAX.toString()).build();
    return sexCodes;
  }

  private StudentEntity getStudentEntity() {
    StudentEntity student = new StudentEntity();
    student.setPen("987654321");
    student.setLegalFirstName("John");
    student.setLegalMiddleNames("Duke");
    student.setLegalLastName("Wayne");
    student.setDob(LocalDate.parse("1907-05-26"));
    student.setSexCode("M");
    student.setGenderCode(null);
    student.setUsualFirstName("Johnny");
    student.setUsualMiddleNames("Duke");
    student.setUsualLastName("Wayne");
    student.setEmail("theduke@someplace.com");
    student.setDeceasedDate(LocalDate.parse("1979-06-11"));
    return student;
  }
}
