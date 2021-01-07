package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.repository.v1.*;
import ca.bc.gov.educ.api.student.service.v1.CodeTableService;
import ca.bc.gov.educ.api.student.service.v1.StudentHistoryService;
import ca.bc.gov.educ.api.student.service.v1.StudentService;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.struct.v1.StudentUpdate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudentServiceTest {
  private static final StudentMapper mapper = StudentMapper.mapper;

  @Autowired
  StudentRepository repository;
  StudentService service;

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
  StudentTwinRepository studentTwinRepo;
  @Autowired
  StudentHistoryRepository studentHistoryRepository;
  StudentHistoryService studentHistoryService;
  @Mock
  CodeTableService codeTableService;

  @Before
  public void before() {
    studentHistoryService = new StudentHistoryService(studentHistoryRepository, codeTableService);
    service = new StudentService(repository, studentMergeRepo, studentTwinRepo, codeTableService, studentHistoryService);
  }

  @Test
  public void testCreateStudent_WhenPayloadIsValid_ShouldReturnSavedObject() {
    StudentEntity student = service.createStudent(getStudentCreate());
    assertNotNull(student);
    assertNotNull(student.getStudentID());

    var history = studentHistoryRepository.findByStudentID(student.getStudentID(), PageRequest.of(0, 10));
    assertThat(history.getTotalElements()).isEqualTo(1);
    assertThat(history.getContent().get(0).getHistoryActivityCode()).isEqualTo("USERNEW");
    assertThat(history.getContent().get(0).getCreateUser()).isEqualTo(student.getCreateUser());
    assertThat(history.getContent().get(0).getLegalFirstName()).isEqualTo(student.getLegalFirstName());
  }

  @Test
  public void testCreateStudentNoGiven_WhenPayloadIsValid_ShouldReturnSavedObject() {
    var student = getStudentCreate();
    student.setLegalFirstName(null);
    var entity = service.createStudent(student);
    assertNotNull(entity);
    assertNotNull(entity.getStudentID());
  }

  @Test
  public void testRetrieveStudent_WhenStudentExistInDB_ShouldReturnStudent() {
    StudentEntity student = service.createStudent(getStudentCreate());
    assertNotNull(student);
    assertNotNull(service.retrieveStudent(student.getStudentID()));
  }

  @Test
  public void testRetrieveStudent_WhenStudentDoesNotExistInDB_ShouldThrowEntityNotFoundException() {
    var studentID = UUID.fromString("00000000-0000-0000-0000-f3b2d4f20000");
    assertThrows(EntityNotFoundException.class, () -> service.retrieveStudent(studentID));
  }

  @Test
  public void testUpdateStudent_WhenPayloadIsValid_ShouldReturnTheUpdatedObject() {

    StudentEntity student = service.createStudent(getStudentCreate());
    student.setLegalFirstName("updatedFirstName");

    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(student.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("USEREDIT");
    studentUpdate.setUpdateUser("Test Update");
    BeanUtils.copyProperties(StudentMapper.mapper.toStructure(student), studentUpdate);
    StudentEntity updateEntity = service.updateStudent(studentUpdate, UUID.fromString(studentUpdate.getStudentID()));
    assertNotNull(updateEntity);
    assertThat(updateEntity.getLegalFirstName()).isEqualTo("updatedFirstName".toUpperCase());

    var history = studentHistoryRepository.findByStudentID(student.getStudentID(), PageRequest.of(0, 10));
    assertThat(history.getTotalElements()).isEqualTo(2);
    assertThat(history.getContent().get(1).getHistoryActivityCode()).isEqualTo("USEREDIT");
    assertThat(history.getContent().get(1).getCreateUser()).isEqualTo(studentUpdate.getUpdateUser());
    assertThat(history.getContent().get(1).getLegalFirstName()).isEqualTo(studentUpdate.getLegalFirstName().toUpperCase());

  }

  @Test(expected = EntityNotFoundException.class)
  public void testUpdateStudent_WhenStudentNotExist_ShouldThrowException() {

    StudentEntity student = getStudentEntity();
    student.setStudentID(UUID.randomUUID());
    student.setLegalFirstName("updatedFirstName");

    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(student.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("USEREDIT");
    BeanUtils.copyProperties(StudentMapper.mapper.toStructure(student), studentUpdate);
    service.updateStudent(studentUpdate, UUID.fromString(studentUpdate.getStudentID()));
  }

  @Test
  public void testFindAllStudent_WhenPayloadIsValid_ShouldReturnAllStudentsObject() throws ExecutionException, InterruptedException {
    assertNotNull(service.findAll(null, 0, 5, new ArrayList<>()).get());
  }

  @Test(expected = Exception.class)
  public void testFindAllStudent_WhenStudentSpecsIsValid_ShouldThrowException() throws ExecutionException, InterruptedException {
    var repository = mock(StudentRepository.class);
    when(repository.findAll(isNull(), any(Pageable.class))).thenThrow(EntityNotFoundException.class);
    var service = new StudentService(repository, studentMergeRepo, studentTwinRepo, codeTableService, studentHistoryService);
    service.findAll(null, 0, 5, new ArrayList<>()).get();
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
    student.setCreateUser("Test");
    student.setUpdateUser("Test Update");
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
