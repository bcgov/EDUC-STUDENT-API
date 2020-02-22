package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudentServiceTest {

  @Autowired
  StudentRepository repository;
  StudentService service;

  @Before
  public void before() {
    service = new StudentService(repository);
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


  private StudentEntity getStudentEntity() {
    StudentEntity student = new StudentEntity();
    student.setPen("987654321");
    student.setLegalFirstName("John");
    student.setLegalMiddleNames("Duke");
    student.setLegalLastName("Wayne");
    student.setDob(LocalDate.parse("1907-05-26"));
    student.setGenderCode('M');
    student.setSexCode('M');
    student.setDataSourceCode("MYED");
    student.setUsualFirstName("Johnny");
    student.setUsualMiddleNames("Duke");
    student.setUsualLastName("Wayne");
    student.setEmail("theduke@someplace.com");
    student.setDeceasedDate(LocalDate.parse("1979-06-11"));
    return student;
  }
}
