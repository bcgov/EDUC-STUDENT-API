package ca.bc.gov.educ.api.student.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.repository.v1.DemogCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.v1.GenderCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.v1.GradeCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.v1.SexCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.v1.StatusCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentRepository;
import ca.bc.gov.educ.api.student.service.v1.CodeTableService;
import ca.bc.gov.educ.api.student.service.v1.StudentHistoryService;
import ca.bc.gov.educ.api.student.service.v1.StudentService;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.struct.v1.StudentUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
public class StudentServiceTest {
  private static final StudentMapper mapper = StudentMapper.mapper;

  @Autowired
  StudentRepository repository;
  @Autowired
  StudentEventRepository studentEventRepository;
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
  StudentHistoryRepository studentHistoryRepository;
  StudentHistoryService studentHistoryService;
  @Mock
  CodeTableService codeTableService;

  @Before
  public void before() {
    studentHistoryService = new StudentHistoryService(studentHistoryRepository, codeTableService);
    service = new StudentService(studentEventRepository, repository, codeTableService, studentHistoryService);
  }

  @Test
  public void testCreateStudent_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
    StudentEntity student = service.createStudent(getStudentCreate()).getLeft();
    assertNotNull(student);
    assertNotNull(student.getStudentID());

    var history = studentHistoryRepository.findByStudentID(student.getStudentID(), PageRequest.of(0, 10));
    assertThat(history.getTotalElements()).isEqualTo(1);
    assertThat(history.getContent().get(0).getHistoryActivityCode()).isEqualTo("USERNEW");
    assertThat(history.getContent().get(0).getCreateUser()).isEqualTo(student.getCreateUser());
    assertThat(history.getContent().get(0).getLegalFirstName()).isEqualTo(student.getLegalFirstName());
  }
  @Test
  public void testCreateStudent_WhenPayloadIsValidAndContainsSpacesAtTheEnd_ShouldReturnSavedObjectTrimmed() throws JsonProcessingException {
    val studentCreate = getStudentCreate();
    studentCreate.setLegalFirstName("TEST SPACE AT THE END    ");
    studentCreate.setLegalLastName("TEST SPACE AT THE END    ");
    studentCreate.setUsualFirstName("TEST SPACE AT THE END    ");
    studentCreate.setUsualLastName("TEST SPACE AT THE END    ");
    studentCreate.setLegalMiddleNames("TEST SPACE AT THE END    ");
    studentCreate.setUsualMiddleNames("TEST SPACE AT THE END    ");
    StudentEntity student = service.createStudent(studentCreate).getLeft();
    assertNotNull(student);
    assertNotNull(student.getStudentID());
    assertThat(student.getLegalFirstName()).isEqualTo("TEST SPACE AT THE END");
    assertThat(student.getLegalLastName()).isEqualTo("TEST SPACE AT THE END");
    assertThat(student.getUsualFirstName()).isEqualTo("TEST SPACE AT THE END");
    assertThat(student.getUsualLastName()).isEqualTo("TEST SPACE AT THE END");
    assertThat(student.getLegalMiddleNames()).isEqualTo("TEST SPACE AT THE END");
    assertThat(student.getUsualMiddleNames()).isEqualTo("TEST SPACE AT THE END");
    var history = studentHistoryRepository.findByStudentID(student.getStudentID(), PageRequest.of(0, 10));
    assertThat(history.getTotalElements()).isEqualTo(1);
    assertThat(history.getContent().get(0).getHistoryActivityCode()).isEqualTo("USERNEW");
    assertThat(history.getContent().get(0).getCreateUser()).isEqualTo(student.getCreateUser());
    assertThat(history.getContent().get(0).getLegalFirstName()).isEqualTo(student.getLegalFirstName());
  }

  @Test
  public void testCreateStudentNoGiven_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
    var student = getStudentCreate();
    student.setLegalFirstName(null);
    var entity = service.createStudent(student).getLeft();
    assertNotNull(entity);
    assertNotNull(entity.getStudentID());
    assertNotNull(entity.getTrueStudentID());
  }

  @Test
  public void testRetrieveStudent_WhenStudentExistInDB_ShouldReturnStudent() throws JsonProcessingException {
    StudentEntity student = service.createStudent(getStudentCreate()).getLeft();
    assertNotNull(student);
    assertNotNull(service.retrieveStudent(student.getStudentID()));
  }

  @Test
  public void testRetrieveStudent_WhenStudentDoesNotExistInDB_ShouldThrowEntityNotFoundException() {
    var studentID = UUID.fromString("00000000-0000-0000-0000-f3b2d4f20000");
    assertThrows(EntityNotFoundException.class, () -> service.retrieveStudent(studentID));
  }

  @Test
  public void testUpdateStudent_WhenPayloadIsValid_ShouldReturnTheUpdatedObject() throws JsonProcessingException {

    StudentEntity student = service.createStudent(getStudentCreate()).getLeft();
    student.setLegalFirstName("updatedFirstName");
    var trueStudentID = UUID.randomUUID();
    student.setTrueStudentID(trueStudentID);

    var studentUpdate = new StudentUpdate();
    studentUpdate.setStudentID(student.getStudentID().toString());
    studentUpdate.setHistoryActivityCode("USEREDIT");
    studentUpdate.setUpdateUser("Test Update");
    BeanUtils.copyProperties(StudentMapper.mapper.toStructure(student), studentUpdate);
    StudentEntity updateEntity = service.updateStudent(studentUpdate, UUID.fromString(studentUpdate.getStudentID())).getLeft();
    assertNotNull(updateEntity);
    assertThat(updateEntity.getLegalFirstName()).isEqualTo("updatedFirstName".toUpperCase());

    var history = studentHistoryRepository.findByStudentID(student.getStudentID(), PageRequest.of(0, 10));
    assertThat(history.getTotalElements()).isEqualTo(2);
    assertThat(history.getContent().get(1).getHistoryActivityCode()).isEqualTo("USEREDIT");
    assertThat(history.getContent().get(1).getCreateUser()).isEqualTo(studentUpdate.getUpdateUser());
    assertThat(history.getContent().get(1).getLegalFirstName()).isEqualTo(studentUpdate.getLegalFirstName().toUpperCase());
    assertThat(history.getContent().get(1).getTrueStudentID()).isEqualTo(trueStudentID);

  }

  @Test(expected = EntityNotFoundException.class)
  public void testUpdateStudent_WhenStudentNotExist_ShouldThrowException() throws JsonProcessingException {

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
    student.setTrueStudentID(UUID.randomUUID());
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
