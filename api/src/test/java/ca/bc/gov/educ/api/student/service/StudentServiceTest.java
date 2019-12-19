package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class StudentServiceTest {

    @Autowired
    StudentService service;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");

    @Test
    public void createValidDigitalIdTest() throws ParseException {

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

        assertNotNull(service.createStudent(student));
    }

    @Test
    public void createDigitalIdThrowsExceptionWhenIDGivenTest() throws ParseException{
        StudentEntity student = new StudentEntity();
        student.setStudentID(UUID.fromString("00000000-8000-0000-000e-000000000000"));
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

        assertThrows(InvalidParameterException.class, () -> {
            service.createStudent(student);
        });
    }

    @Test
    public void createDigitalIdThrowsExceptionWhenCreateUserGivenTest() throws ParseException{
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
        student.setCreateUser("USER");

        assertThrows(InvalidParameterException.class, () -> {
            service.createStudent(student);
        });
    }

    @Test
    public void retrieveValidStudentIdTest() throws ParseException{
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

        service.createStudent(student);

        assertNotNull(service.retrieveStudent(student.getStudentID()));
    }

    @Test
    public void retrieveInvalidStudentIdTest(){
        assertThrows(EntityNotFoundException.class, () -> {
            service.retrieveStudent(UUID.fromString("00000000-8000-0000-000e-000000000000"));
        });
    }

    @Test
    public void updateValidDigitalIdTest() throws ParseException{

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
        service.createStudent(student);

        StudentEntity newStudent = new StudentEntity();
        newStudent.setStudentID(student.getStudentID());
        newStudent.setPen("987654321");
        newStudent.setLegalFirstName("Johnathon");
        newStudent.setLegalMiddleNames("Duke");
        newStudent.setLegalLastName("Wayne");
        newStudent.setDob(formatter.parse("1907-05-26"));
        newStudent.setGenderCode('M');
        student.setSexCode('M');
        newStudent.setDataSourceCode("MYED");
        newStudent.setUsualFirstName("Johnny");
        newStudent.setUsualMiddleNames("Duke");
        newStudent.setUsualLastName("Wayne");
        newStudent.setEmail("theduke@someplace.com");
        newStudent.setDeceasedDate(formatter.parse("1979-06-11"));

        assertNotNull(service.updateStudent(newStudent));
    }
}
