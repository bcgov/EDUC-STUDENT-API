package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentMergeEntity;
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
public class StudentMergeServiceTest {

  @Autowired
  StudentRepository repository;

  StudentService studentService;

  StudentMergeService studentMergeService;

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
  StudentMergeRepository studentMergeRepo;

  @Autowired
  StudentMergeDirectionCodeTableRepository studentMergeDirectionRepo;

  @Autowired
  StudentMergeSourceCodeTableRepository studentMergeSourceRepo;

  @Before
  public void before() {
    studentService = new StudentService(repository, genderRepo, sexRepo, statusRepo, demogRepo, gradeRepo);
    studentMergeService = new StudentMergeService(studentMergeRepo, studentMergeDirectionRepo, studentMergeSourceRepo);
  }

  @Test
  public void testFindStudentMerges_WhenStudentMergesDoNotExistInDB_ShouldReturnEmptyList() {
    StudentEntity student = getStudentEntity();
    assertNotNull(studentService.createStudent(student));
    assertThat(studentMergeService.findStudentMerges(student.getStudentID()).size()).isZero();
  }

  @Test
  public void testFindStudentMerges_WhenStudentMergesExistInDB_ShouldReturnList() {
    StudentEntity student = getStudentEntity();
    assertNotNull(studentService.createStudent(student));
    StudentEntity mergedStudent = getStudentEntity();
    assertNotNull(studentService.createStudent(mergedStudent));
    StudentMergeEntity studentMerge = new StudentMergeEntity();
    studentMerge.setStudentID(student.getStudentID());
    studentMerge.setMergeStudent(mergedStudent);
    studentMerge.setStudentMergeDirectionCode("FROM");
    studentMerge.setStudentMergeSourceCode("MINISTRY");
    assertNotNull(studentMergeService.createStudentMerge(studentMerge));
    assertThat(studentMergeService.findStudentMerges(student.getStudentID()).size()).isEqualTo(1);
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
