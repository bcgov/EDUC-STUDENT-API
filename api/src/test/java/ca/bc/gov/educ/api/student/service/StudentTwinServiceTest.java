package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.struct.StudentCreate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudentTwinServiceTest {
  private static final StudentMapper mapper = StudentMapper.mapper;

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

  @Autowired
  StudentRepository studentRepository;

  @Mock
  StudentHistoryService studentHistoryService;

  @Mock
  CodeTableService codeTableService;

  @Before
  public void before() {
    studentService = new StudentService(repository, studentMergeRepo, studentTwinRepo, codeTableService, studentHistoryService);
    studentTwinService = new StudentTwinService(studentTwinRepo, studentService, studentTwinReasonRepo);
  }

  @Test
  public void testFindStudentTwins_WhenStudentTwinsDoNotExistInDB_ShouldReturnEmptyList() {
    StudentEntity student = studentService.createStudent(getStudentCreate());
    assertNotNull(student);
    assertThat(studentTwinService.findStudentTwins(student.getStudentID()).size()).isZero();
  }

  @Test
  public void testFindStudentTwins_WhenStudentTwinsExistInDB_ShouldReturnList() {
    StudentEntity student = studentService.createStudent(getStudentCreate());
    assertNotNull(student);
    StudentEntity twinedStudent = studentService.createStudent(getStudentCreate());
    assertNotNull(twinedStudent);
    StudentTwinEntity studentTwin = new StudentTwinEntity();
    studentTwin.setStudentID(student.getStudentID());
    studentTwin.setTwinStudentID(twinedStudent.getStudentID());
    studentTwin.setStudentTwinReasonCode("PENMATCH");
    assertNotNull(studentTwinService.createStudentTwin(studentTwin));
    assertThat(studentTwinService.findStudentTwins(student.getStudentID()).size()).isEqualTo(1);
  }

  @Test
  public void testFindStudentTwins_WhenStudentTwinsOtherExistInDB_ShouldReturnList() {
    StudentEntity student = studentService.createStudent(getStudentCreate());
    assertNotNull(student);
    StudentEntity twinedStudent = studentService.createStudent(getStudentCreate());
    assertNotNull(twinedStudent);
    StudentTwinEntity studentTwin = new StudentTwinEntity();
    studentTwin.setStudentID(twinedStudent.getStudentID());
    studentTwin.setTwinStudentID(student.getStudentID());
    studentTwin.setStudentTwinReasonCode("PENMATCH");
    assertNotNull(studentTwinService.createStudentTwin(studentTwin));
    assertThat(studentTwinService.findStudentTwins(student.getStudentID()).size()).isEqualTo(1);
  }

  @Test
  public void testDeleteStudentTwin_ShouldReturnTrue() {
    StudentEntity student = studentService.createStudent(getStudentCreate());
    assertNotNull(student);
    StudentEntity twinedStudent = studentService.createStudent(getStudentCreate());
    assertNotNull(twinedStudent);
    StudentTwinEntity studentTwin = new StudentTwinEntity();
    studentTwin.setStudentID(student.getStudentID());
    studentTwin.setTwinStudentID(twinedStudent.getStudentID());
    studentTwin.setStudentTwinReasonCode("PENMATCH");
    assertNotNull(studentTwinService.createStudentTwin(studentTwin));
    List<StudentTwinEntity> twins = studentTwinService.findStudentTwins(student.getStudentID());
    assertThat(twins.size()).isEqualTo(1);
    studentTwinService.deleteById(twins.get(0).getStudentTwinID());
    assertThat(studentTwinService.findStudentTwins(student.getStudentID()).size()).isZero();
  }

  @Test
  public void testDeleteStudentTwinByIDs_ShouldReturnTrue() {
    StudentEntity student = studentService.createStudent(getStudentCreate());
    assertNotNull(student);
    var studentTwins = IntStream.rangeClosed(1, 3).mapToObj(count -> {
      var twinedStudent = studentService.createStudent(getStudentCreate());
      var studentTwin = new StudentTwinEntity();
      studentTwin.setStudentID(student.getStudentID());
      studentTwin.setTwinStudentID(twinedStudent.getStudentID());
      studentTwin.setStudentTwinReasonCode("PENMATCH");
      return studentTwin;
    }).collect(Collectors.toList()); ;


    studentTwinService.addStudentTwins(studentTwins);
    List<StudentTwinEntity> twins = studentTwinService.findStudentTwins(student.getStudentID());
    assertThat(twins.size()).isEqualTo(3);
    studentTwinService.deleteAllByIds(twins.stream().map(StudentTwinEntity::getStudentTwinID).collect(Collectors.toList()));
    assertThat(studentTwinService.findStudentTwins(student.getStudentID()).size()).isZero();
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

  private StudentCreate getStudentCreate() {
    var studentEntity = getStudentEntity();
    var studentCreate = new StudentCreate();
    BeanUtils.copyProperties(mapper.toStructure(studentEntity), studentCreate);
    studentCreate.setHistoryActivityCode("USERNEW");
    return studentCreate;
  }
}
