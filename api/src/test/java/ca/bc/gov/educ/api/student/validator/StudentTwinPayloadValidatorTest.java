package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.service.CodeTableService;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.service.StudentTwinService;
import ca.bc.gov.educ.api.student.struct.StudentTwin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StudentTwinPayloadValidatorTest {
  @Mock
  StudentRepository repository;

  @Mock
  GenderCodeTableRepository genderRepo;

  @Mock
  SexCodeTableRepository sexRepo;

  @Mock
  DemogCodeTableRepository demogRepo;

  @Mock
  StatusCodeTableRepository statusRepo;

  @Mock
  GradeCodeTableRepository gradeRepo;

  @Mock
  StudentTwinRepository studentTwinRepo;

  @Mock
  StudentMergeRepository studentMergeRepo;

  @Mock
  StudentTwinReasonCodeTableRepository studentTwinReasonCodeTableRepo;

  @Mock
  StudentService studentService;

  @Mock
  StudentTwinService studentTwinService;

  @InjectMocks
  StudentTwinPayloadValidator studentTwinPayloadValidator;

  @Mock
  CodeTableService codeTableService;

  @Before
  public void before() {
    studentService = new StudentService(repository, studentMergeRepo, studentTwinRepo,codeTableService);
    studentTwinService = new StudentTwinService(studentTwinRepo, studentService, studentTwinReasonCodeTableRepo);
    studentTwinPayloadValidator = new StudentTwinPayloadValidator(studentTwinService, studentService);
  }

  @Test
  public void testValidateTwinReasonCode_WhenTwinReasonCodeIsInvalid_ShouldAddAnErrorToTheReturnedList() {
    StudentTwin twin = StudentTwin.builder().studentTwinReasonCode("WRONG_CODE").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentTwinReasonCodeTableRepo.findAll()).thenReturn(createDummyStudentTwinReasonCodeRecords());
    studentTwinPayloadValidator.validateTwinReasonCode(twin, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateTwinReasonCode_WhenEffectiveDateIsAfterCurrentTime_ShouldAddAnErrorToTheReturnedList() {
    StudentTwin twin = StudentTwin.builder().studentTwinReasonCode("PENMATCH").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentTwinReasonCodeTableRepo.findAll()).thenReturn(createDummyStudentTwinReasonCodeRecords(LocalDateTime.MAX, LocalDateTime.MAX));
    studentTwinPayloadValidator.validateTwinReasonCode(twin, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateTwinReasonCode_WhenExpiryDateisBeforeCurrentTime_ShouldAddAnErrorToTheReturnedList() {
    StudentTwin twin = StudentTwin.builder().studentTwinReasonCode("PENMATCH").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentTwinReasonCodeTableRepo.findAll()).thenReturn(createDummyStudentTwinReasonCodeRecords(LocalDateTime.now(), LocalDateTime.MIN));
    studentTwinPayloadValidator.validateTwinReasonCode(twin, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateStudentID_WhenStudentIdIsNotExisted_ShouldNotAddAnErrorToTheReturnedList() {
    var studentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20000";
    StudentTwin twin = StudentTwin.builder().studentID(studentID).build();
    List<FieldError> errorList = new ArrayList<>();
    when(repository.findById(UUID.fromString(studentID))).thenReturn(Optional.empty());
    studentTwinPayloadValidator.validateStudentID(studentID, twin, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeStudentID_WhenMergeStudentIdIsNotExisted_ShouldNotAddAnErrorTOTheReturnedList() {
    var studentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20000";
    StudentTwin twin = StudentTwin.builder().twinStudentID(studentID).build();
    StudentTwinEntity twinEntity = new StudentTwinEntity();
    List<FieldError> errorList = new ArrayList<>();
    when(repository.findById(UUID.fromString(studentID))).thenReturn(Optional.empty());
    studentTwinPayloadValidator.validateTwinStudentID(twin, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidatePayload_WhenMergeStudentIdIsNotExisted_ShouldNotAddAnErrorTOTheReturnedList() {
    var studentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20000";
    var twinStudentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20001";
    StudentTwin twin = StudentTwin.builder().studentID(studentID).twinStudentID(twinStudentID).
            studentTwinReasonCode("PENMATCH").studentTwinID("123455678").build();
    StudentTwinEntity twinEntity = new StudentTwinEntity();
    when(repository.findById(UUID.fromString(studentID))).thenReturn(createDummyStudentRecord(studentID));
    when(repository.findById(UUID.fromString(twinStudentID))).thenReturn(createDummyStudentRecord(twinStudentID));
    when(studentTwinReasonCodeTableRepo.findAll()).thenReturn(createDummyStudentTwinReasonCodeRecords());

    var errorList = studentTwinPayloadValidator.validatePayload(studentID, twin, true);
    assertEquals(1, errorList.size());
  }

  private List<StudentTwinReasonCodeEntity> createDummyStudentTwinReasonCodeRecords(LocalDateTime effectiveDate, LocalDateTime expiryDate) {
    return List.of(StudentTwinReasonCodeEntity.builder().twinReasonCode("PENMATCH").effectiveDate(effectiveDate).expiryDate(expiryDate).build(),
            StudentTwinReasonCodeEntity.builder().twinReasonCode("PENCREATE").effectiveDate(effectiveDate).expiryDate(expiryDate).build());
  }

  private List<StudentTwinReasonCodeEntity> createDummyStudentTwinReasonCodeRecords() {
    return createDummyStudentTwinReasonCodeRecords(LocalDateTime.now(), LocalDateTime.MAX);
  }

  private Optional<StudentEntity> createDummyStudentRecord(String studentID) {
    StudentEntity entity = new StudentEntity();
    entity.setPen("123456780");
    entity.setEmail("abc@gmail.com");
    entity.setStudentID(UUID.fromString(studentID));
    return Optional.of(entity);
  }
}
