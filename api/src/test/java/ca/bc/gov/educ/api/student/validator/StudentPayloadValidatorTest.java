package ca.bc.gov.educ.api.student.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.bc.gov.educ.api.student.model.GenderCodeEntity;
import ca.bc.gov.educ.api.student.model.SexCodeEntity;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.service.StudentMergeService;
import ca.bc.gov.educ.api.student.service.StudentTwinService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.FieldError;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.Student;

@RunWith(MockitoJUnitRunner.class)
public class StudentPayloadValidatorTest {
  private boolean isCreateOperation = false;
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
  StudentService studentService;
  @Mock
  StudentTwinService studentTwinService;
  @Mock
  StudentMergeService studentMergeService;
  @InjectMocks
  StudentPayloadValidator studentPayloadValidator;
  @Mock
  StudentMergeRepository studentMergeRepo;
  @Mock
  StudentTwinRepository studentTwinRepo;

  @Before
  public void before() {
    studentService = new StudentService(repository, studentMergeRepo, studentTwinRepo, genderRepo, sexRepo, statusRepo, demogRepo, gradeRepo);
    studentPayloadValidator = new StudentPayloadValidator(studentService, studentTwinService, studentMergeService);
  }

  @Test
  public void testValidatePEN_WhenPENNumberInThePayloadIsAlreadyAssociatedToDifferentStudentForInsertOperation_ShouldAddAnErrorTOTheReturnedList() {
    isCreateOperation = true;
    final String pen = "123456789";
    List<FieldError> errorList = new ArrayList<>();
    when(repository.findStudentEntityByPen(pen)).thenReturn(createDummyStudentRecordForInsertOperation(pen));
    studentPayloadValidator.validatePEN(Student.builder().pen(pen).build(), isCreateOperation, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidatePEN_WhenPENNumberInThePayloadIsNotAssociatedToDifferentStudentForInsertOperation_ShouldNotAddAnErrorTOTheReturnedList() {
    isCreateOperation = true;
    final String pen = "123456780";
    List<FieldError> errorList = new ArrayList<>();
    createDummyStudentRecordForInsertOperation(pen);
    when(repository.findStudentEntityByPen(pen)).thenReturn(Optional.empty());
    studentPayloadValidator.validatePEN(Student.builder().pen(pen).build(), isCreateOperation, errorList);
    assertEquals(0, errorList.size());
  }

  @Test
  public void testValidatePEN_WhenPENNumberInThePayloadIsAssociatedToDifferentStudentForUpdateOperation_ShouldAddAnErrorTOTheReturnedList() {
    isCreateOperation = false;
    final String pen = "123456780";
    List<FieldError> errorList = new ArrayList<>();
    when(repository.findStudentEntityByPen(pen)).thenReturn(createDummyStudentRecordForUpdateOperation());
    studentPayloadValidator.validatePEN(Student.builder().pen(pen).studentID("8e20a9c8-6ff3-12bf-816f-f3b2d4f20001").build(), isCreateOperation, errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidatePEN_WhenPENNumberInThePayloadIsAssociatedToSameStudentForUpdateOperation_ShouldNotAddAnErrorTOTheReturnedList() {
    isCreateOperation = false;
    final String pen = "123456780";
    List<FieldError> errorList = new ArrayList<>();
    when(repository.findStudentEntityByPen(pen)).thenReturn(createDummyStudentRecordForUpdateOperation());
    studentPayloadValidator.validatePEN(Student.builder().pen(pen).studentID("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000").build(), isCreateOperation, errorList);
    assertEquals(0, errorList.size());
  }


  @Test
  public void testValidateGenderCode_WhenGenderCodeDoesNotExistInCodeTable_ShouldAddAnErrorTOTheReturnedList() {
    final String pen = "123456789";
    List<FieldError> errorList = new ArrayList<>();
    studentPayloadValidator.validateGenderCode(Student.builder().genderCode("M").pen(pen).build(), errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateGenderCode_WhenGenderCodeExistInCodeTableButEffectiveDateIsFutureDate_ShouldAddAnErrorTOTheReturnedList() {
    final String pen = "123456789";
    List<FieldError> errorList = new ArrayList<>();
    GenderCodeEntity code = dummyGenderCode();
    code.setEffectiveDate(LocalDateTime.MAX);
    code.setGenderCode("M");
    when(genderRepo.findAll()).thenReturn(List.of(code));
    studentPayloadValidator.validateGenderCode(Student.builder().genderCode("M").pen(pen).build(), errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateGenderCode_WhenGenderCodeExistInCodeTableButExpiryDateIsPast_ShouldAddAnErrorTOTheReturnedList() {
    final String pen = "123456789";
    List<FieldError> errorList = new ArrayList<>();
    GenderCodeEntity code = dummyGenderCode();
    code.setExpiryDate(LocalDateTime.MIN);
    code.setGenderCode("M");
    when(genderRepo.findAll()).thenReturn(List.of(code));
    studentPayloadValidator.validateGenderCode(Student.builder().genderCode("M").pen(pen).build(), errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateSexCode_WhenSexCodeDoesNotExistInCodeTable_ShouldAddAnErrorTOTheReturnedList() {
    final String pen = "123456789";
    List<FieldError> errorList = new ArrayList<>();
    studentPayloadValidator.validateSexCode(Student.builder().sexCode("M").pen(pen).build(), errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateSexCode_WhenSexCodeExistInCodeTableButEffectiveDateIsFutureDate_ShouldAddAnErrorTOTheReturnedList() {
    final String pen = "123456789";
    List<FieldError> errorList = new ArrayList<>();
    SexCodeEntity code = dummySexCode();
    code.setEffectiveDate(LocalDateTime.MAX);
    code.setSexCode("M");
    when(sexRepo.findAll()).thenReturn(List.of(code));
    studentPayloadValidator.validateSexCode(Student.builder().sexCode("M").pen(pen).build(), errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidateSexCode_WhenSexCodeExistInCodeTableButExpiryDateIsPast_ShouldAddAnErrorTOTheReturnedList() {
    final String pen = "123456789";
    List<FieldError> errorList = new ArrayList<>();
    SexCodeEntity code = dummySexCode();
    code.setExpiryDate(LocalDateTime.MIN);
    code.setSexCode("M");
    when(sexRepo.findAll()).thenReturn(List.of(code));
    studentPayloadValidator.validateSexCode(Student.builder().sexCode("M").pen(pen).build(), errorList);
    assertEquals(1, errorList.size());
  }

  @Test
  public void testValidatePayload_WhenAllTheFieldsAreInvalidForCreate_ShouldAddAllTheErrorsTOTheReturnedList() {
    isCreateOperation = true;
    final String pen = "123456789";
    when(repository.findStudentEntityByPen(pen)).thenReturn(createDummyStudentRecordForInsertOperation(pen));
    List<FieldError> errorList = studentPayloadValidator.validatePayload(Student.builder().studentID("8e20a9c8-6ff3-12bf-816f-f3b2d4f20001").genderCode("M").pen(pen).build(), isCreateOperation);
    assertEquals(3, errorList.size());
  }

  private SexCodeEntity dummySexCode() {
    return SexCodeEntity.builder().sexCode("M").effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).build();
  }

  private GenderCodeEntity dummyGenderCode() {
    return GenderCodeEntity.builder().genderCode("M").effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).build();
  }

  private Optional<StudentEntity> createDummyStudentRecordForInsertOperation(String pen) {
    StudentEntity entity = new StudentEntity();
    entity.setPen(pen);
    entity.setEmail("abc@gmail.com");
    return Optional.of(entity);
  }

  private Optional<StudentEntity> createDummyStudentRecordForUpdateOperation() {
    StudentEntity entity = new StudentEntity();
    entity.setPen("123456780");
    entity.setEmail("abc@gmail.com");
    entity.setStudentID(UUID.fromString("8e20a9c8-6ff3-12bf-816f-f3b2d4f20000"));
    return Optional.of(entity);
  }


}
