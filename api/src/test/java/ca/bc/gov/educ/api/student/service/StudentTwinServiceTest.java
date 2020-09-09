package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import ca.bc.gov.educ.api.student.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudentTwinServiceTest {

  @Autowired
  StudentRepository repository;

  StudentService studentService;

  StudentTwinService studentTwinService;

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
  StudentTwinRepository studentTwinRepo;

  @Autowired
  StudentTwinReasonCodeTableRepository studentTwinReasonRepo;

  @Autowired
  StudentMergeRepository studentMergeRepo;
  @Before
  public void before() {
    studentService = new StudentService(repository, studentMergeRepo, studentTwinRepo, genderRepo, sexRepo, statusRepo, demogRepo, gradeRepo);
    studentTwinService = new StudentTwinService(studentTwinRepo, studentTwinReasonRepo);
  }

  @Test
  public void testFindStudentTwins_WhenStudentTwinsDoNotExistInDB_ShouldReturnEmptyList() {
    StudentEntity student = getStudentEntity();
    assertNotNull(studentService.createStudent(student));
    assertThat(studentTwinService.findStudentTwins(student.getStudentID()).size()).isZero();
  }

  @Test
  public void testFindStudentTwins_WhenStudentTwinsExistInDB_ShouldReturnList() {
    StudentEntity student = getStudentEntity();
    assertNotNull(studentService.createStudent(student));
    StudentEntity twinedStudent = getStudentEntity();
    assertNotNull(studentService.createStudent(twinedStudent));
    StudentTwinEntity studentTwin = new StudentTwinEntity();
    studentTwin.setStudentID(student.getStudentID());
    studentTwin.setTwinStudent(twinedStudent);
    studentTwin.setStudentTwinReasonCode("PENMATCH");
    assertNotNull(studentTwinService.createStudentTwin(studentTwin));
    assertThat(studentTwinService.findStudentTwins(student.getStudentID()).size()).isEqualTo(1);
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
    student.setStatusCode("A");
    student.setDemogCode("A");
    student.setUsualFirstName("Johnny");
    student.setUsualMiddleNames("Duke");
    student.setUsualLastName("Wayne");
    student.setEmail("theduke@someplace.com");
    student.setEmailVerified("Y");
    student.setDeceasedDate(LocalDate.parse("1979-06-11"));
    return student;
  }
}
