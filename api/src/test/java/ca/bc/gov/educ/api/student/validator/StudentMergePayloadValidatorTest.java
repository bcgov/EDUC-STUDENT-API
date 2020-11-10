package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentMergeEntity;
import ca.bc.gov.educ.api.student.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.service.CodeTableService;
import ca.bc.gov.educ.api.student.service.StudentHistoryService;
import ca.bc.gov.educ.api.student.service.StudentMergeService;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.StudentMerge;
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
public class StudentMergePayloadValidatorTest {
  @Mock
  StudentRepository repository;

  @Mock
  StudentMergeRepository studentMergeRepo;

  @Mock
  StudentMergeDirectionCodeTableRepository studentMergeDirectionCodeTableRepo;

  @Mock
  StudentMergeSourceCodeTableRepository studentMergeSourceCodeTableRepo;

  @Mock
  StudentService studentService;

  @Mock
  StudentMergeService studentMergeService;

  @InjectMocks
  StudentMergePayloadValidator studentMergePayloadValidator;
  @Mock
  StudentTwinRepository studentTwinRepo;

  @Mock
  CodeTableService codeTableService;

  @Mock
  StudentHistoryService studentHistoryService;

  @Before
  public void before() {
    studentService = new StudentService(repository, studentMergeRepo, studentTwinRepo, codeTableService, studentHistoryService);
    studentMergeService = new StudentMergeService(studentMergeRepo, studentMergeDirectionCodeTableRepo, studentMergeSourceCodeTableRepo);
    studentMergePayloadValidator = new StudentMergePayloadValidator(studentMergeService, studentService);
  }

  @Test
  public void testValidateMergeDirectionCode_WhenMergeDirectionCodeIsInvalid_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("WRONG_CODE").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeDirectionCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeDirectionCodeRecords());
    studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeDirectionCode_WhenEffectiveDateIsAfterCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("TO").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeDirectionCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeDirectionCodeRecords(LocalDateTime.MAX, LocalDateTime.MAX));
    studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeDirectionCode_WhenExpiryDateisBeforeCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeDirectionCode("FROM").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeDirectionCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeDirectionCodeRecords(LocalDateTime.now(), LocalDateTime.MIN));
    studentMergePayloadValidator.validateMergeDirectionCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenMergeSourceCodeIsNotExisted_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("WRONG_CODE").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeSourceCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeSourceCodeRecords());
    studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenEffectiveDateIsAfterCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("SCHOOL").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeSourceCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeSourceCodeRecords(LocalDateTime.MAX, LocalDateTime.MAX));
    studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeSourceCode_WhenExpiryDateisBeforeCurrentTime_ShouldAddAnErrorTOTheReturnedList() {
    StudentMerge merge = StudentMerge.builder().studentMergeSourceCode("MINISTRY").build();
    List<FieldError> errorList = new ArrayList<>();
    when(studentMergeSourceCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeSourceCodeRecords(LocalDateTime.now(), LocalDateTime.MIN));
    studentMergePayloadValidator.validateMergeSourceCode(merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateStudentID_WhenStudentIdIsNotExisted_ShouldNotAddAnErrorTOTheReturnedList() {
    var studentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20000";
    StudentMerge merge = StudentMerge.builder().studentID(studentID).build();
    List<FieldError> errorList = new ArrayList<>();
    when(repository.findById(UUID.fromString(studentID))).thenReturn(Optional.empty());
    studentMergePayloadValidator.validateStudentID(studentID, merge, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateMergeStudentID_WhenMergeStudentIdIsNotExisted_ShouldNotAddAnErrorTOTheReturnedList() {
    var studentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20000";
    StudentMerge merge = StudentMerge.builder().mergeStudentID(studentID).build();
    StudentMergeEntity mergeEntity = new StudentMergeEntity();
    List<FieldError> errorList = new ArrayList<>();
    when(repository.findById(UUID.fromString(studentID))).thenReturn(Optional.empty());
    studentMergePayloadValidator.validateMergeStudentID(merge, errorList, mergeEntity);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidatePayload_WhenMergeStudentIdIsNotExisted_ShouldNotAddAnErrorTOTheReturnedList() {
    var studentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20000";
    var mergeStudentID = "8e20a9c8-6ff3-12bf-816f-f3b2d4f20001";
    StudentMerge merge = StudentMerge.builder().studentID(studentID).mergeStudentID(mergeStudentID).
        studentMergeDirectionCode("TO").studentMergeSourceCode("SCHOOL").studentMergeID("123455678").build();
    StudentMergeEntity mergeEntity = new StudentMergeEntity();
    when(repository.findById(UUID.fromString(studentID))).thenReturn(createDummyStudentRecord(studentID));
    when(repository.findById(UUID.fromString(mergeStudentID))).thenReturn(createDummyStudentRecord(mergeStudentID));
    when(studentMergeDirectionCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeDirectionCodeRecords());
    when(studentMergeSourceCodeTableRepo.findAll()).thenReturn(createDummyStudentMergeSourceCodeRecords());

    var errorList = studentMergePayloadValidator.validatePayload(studentID, merge, true, mergeEntity);
    assertEquals(1, errorList.size());
  }

  private List<StudentMergeDirectionCodeEntity> createDummyStudentMergeDirectionCodeRecords(LocalDateTime effectiveDate, LocalDateTime expiryDate) {
    return List.of(StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("TO").effectiveDate(effectiveDate).expiryDate(expiryDate).build(),
        StudentMergeDirectionCodeEntity.builder().mergeDirectionCode("FROM").effectiveDate(effectiveDate).expiryDate(expiryDate).build());
  }

  private List<StudentMergeDirectionCodeEntity> createDummyStudentMergeDirectionCodeRecords() {
    return createDummyStudentMergeDirectionCodeRecords(LocalDateTime.now(), LocalDateTime.MAX);
  }

  private List<StudentMergeSourceCodeEntity> createDummyStudentMergeSourceCodeRecords(LocalDateTime effectiveDate, LocalDateTime expiryDate) {
    return List.of(StudentMergeSourceCodeEntity.builder().mergeSourceCode("SCHOOL").effectiveDate(effectiveDate).expiryDate(expiryDate).build(),
        StudentMergeSourceCodeEntity.builder().mergeSourceCode("MINISTRY").effectiveDate(effectiveDate).expiryDate(expiryDate).build());
  }

  private List<StudentMergeSourceCodeEntity> createDummyStudentMergeSourceCodeRecords() {
    return createDummyStudentMergeSourceCodeRecords(LocalDateTime.now(), LocalDateTime.MAX);
  }

  private Optional<StudentEntity> createDummyStudentRecord(String studentID) {
    StudentEntity entity = new StudentEntity();
    entity.setPen("123456780");
    entity.setEmail("abc@gmail.com");
    entity.setStudentID(UUID.fromString(studentID));
    return Optional.of(entity);
  }
}
