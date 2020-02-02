package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StudentServiceTest {

  @InjectMocks
  StudentService service;
  @Mock
  StudentRepository repository;
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");

  @Before
  public void before() {
    service = new StudentService(repository);
  }

  @Test
  public void testCreateStudent_WhenPayloadIsValid_ShouldReturnSavedObject() throws ParseException {

    StudentEntity student = getStudentEntity();
    when(repository.save(student)).thenReturn(student);
    assertNotNull(service.createStudent(student));
  }

  @Test
  public void testCreateStudent_WhenPayloadContainsStudentID_ShouldThrowInvalidParameterException() throws ParseException {
    StudentEntity student = getStudentEntity();
    student.setStudentID(UUID.fromString("00000000-8000-0000-000e-000000000000"));
    assertThrows(InvalidParameterException.class, () -> {
      service.createStudent(student);
    });
  }


  @Test
  public void testRetrieveStudent_WhenStudentExistInDB_ShouldReturnStudent() throws ParseException {
    StudentEntity student = getStudentEntity();
    student.setStudentID(UUID.fromString("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000"));
    when(repository.findById(UUID.fromString("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000"))).thenReturn(Optional.of(student));
    assertNotNull(service.retrieveStudent(student.getStudentID()));
  }

  @Test
  public void testRetrieveStudent_WhenStudentDoesNotExistInDB_ShouldThrowEntityNotFoundException() {
    when(repository.findById(UUID.fromString("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000"))).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.retrieveStudent(UUID.fromString("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000")));
  }

  @Test
  public void testUpdateStudent_WhenPayloadIsValid_ShouldReturnTheUpdatedObject() throws ParseException {

    StudentEntity student = getStudentEntity();
    student.setStudentID(UUID.fromString("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000"));
    when(repository.findById(UUID.fromString("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000"))).thenReturn(Optional.of(student));
    when(repository.save(student)).thenReturn(student);
    StudentEntity updateEntity = service.updateStudent(student);
    assertNotNull(updateEntity);
    assertNotNull(updateEntity.getUpdateDate());
  }


  private StudentEntity getStudentEntity() throws ParseException {
    StudentEntity student = new StudentEntity();
    student.setPen("987654321");
    student.setLegalFirstName("John");
    student.setLegalMiddleNames("Duke");
    student.setLegalLastName("Wayne");
    student.setDob(formatter.parse("1907-05-26"));
    student.setGenderCode('M');
    student.setSexCode('M');
    student.setDataSourceCode("MYED");
    student.setUsualFirstName("Johnny");
    student.setUsualMiddleNames("Duke");
    student.setUsualLastName("Wayne");
    student.setEmail("theduke@someplace.com");
    student.setDeceasedDate(formatter.parse("1979-06-11"));
    return student;
  }
}
