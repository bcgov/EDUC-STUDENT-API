package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.model.v1.GenderCodeEntity;
import ca.bc.gov.educ.api.student.model.v1.SexCodeEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryActivityCodeEntity;
import ca.bc.gov.educ.api.student.service.v1.StudentService;
import ca.bc.gov.educ.api.student.struct.v1.BaseStudent;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.struct.v1.StudentUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * The type Student payload validator.
 */
@Component
public class StudentPayloadValidator {

  /**
   * The constant GENDER_CODE.
   */
  public static final String GENDER_CODE = "genderCode";
  /**
   * The constant SEX_CODE.
   */
  public static final String SEX_CODE = "sexCode";
  /**
   * The constant PEN.
   */
  public static final String PEN = "pen";
  /**
   * The constant HISTORY_ACTIVITY_CODE.
   */
  public static final String HISTORY_ACTIVITY_CODE = "historyActivityCode";
  @Getter(AccessLevel.PRIVATE)
  private final StudentService studentService;

  /**
   * Instantiates a new Student payload validator.
   *
   * @param studentService the student service
   */
  @Autowired
  public StudentPayloadValidator(final StudentService studentService) {
    this.studentService = studentService;
  }

  /**
   * Validate payload list.
   *
   * @param student           the student
   * @param isCreateOperation the is create operation
   * @return the list
   */
  public List<FieldError> validatePayload(BaseStudent student, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && student.getStudentID() != null) {
      apiValidationErrors.add(createFieldError("studentID", student.getStudentID(), "studentID should be null for post operation."));
    }
    validatePEN(student, isCreateOperation, apiValidationErrors);
    validateGenderCode(student, apiValidationErrors);
    validateSexCode(student, apiValidationErrors);
    return apiValidationErrors;
  }

  /**
   * Validate create payload list.
   *
   * @param student the student
   * @return the list
   */
  public List<FieldError> validateCreatePayload(StudentCreate student) {
    var apiValidationErrors = validatePayload(student, true);
    validateStudentHistoryActivityCode(student.getHistoryActivityCode(), apiValidationErrors);
    return apiValidationErrors;
  }

  /**
   * Validate update payload list.
   *
   * @param student the student
   * @return the list
   */
  public List<FieldError> validateUpdatePayload(StudentUpdate student) {
    var apiValidationErrors = validatePayload(student, false);
    validateStudentHistoryActivityCode(student.getHistoryActivityCode(), apiValidationErrors);
    return apiValidationErrors;
  }

  /**
   * Validate gender code.
   *
   * @param student             the student
   * @param apiValidationErrors the api validation errors
   */
  protected void validateGenderCode(BaseStudent student, List<FieldError> apiValidationErrors) {
    if (student.getGenderCode() != null) {
      Optional<GenderCodeEntity> genderCodeEntity = studentService.findGenderCode(student.getGenderCode());
      if (genderCodeEntity.isEmpty()) {
        apiValidationErrors.add(createFieldError(GENDER_CODE, student.getGenderCode(), "Invalid Gender Code."));
      } else if (genderCodeEntity.get().getEffectiveDate() != null && genderCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(GENDER_CODE, student.getGenderCode(), "Gender Code provided is not yet effective."));
      } else if (genderCodeEntity.get().getExpiryDate() != null && genderCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(GENDER_CODE, student.getGenderCode(), "Gender Code provided has expired."));
      }
    }
  }

  /**
   * Validate sex code.
   *
   * @param student             the student
   * @param apiValidationErrors the api validation errors
   */
  protected void validateSexCode(BaseStudent student, List<FieldError> apiValidationErrors) {
    if (student.getSexCode() != null) {
      Optional<SexCodeEntity> sexCodeEntity = studentService.findSexCode(student.getSexCode());
      if (sexCodeEntity.isEmpty()) {
        apiValidationErrors.add(createFieldError(SEX_CODE, student.getSexCode(), "Invalid Sex Code."));
      } else if (sexCodeEntity.get().getEffectiveDate() != null && sexCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(SEX_CODE, student.getSexCode(), "Sex Code provided is not yet effective."));
      } else if (sexCodeEntity.get().getExpiryDate() != null && sexCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(SEX_CODE, student.getSexCode(), "Sex Code provided has expired."));
      }
    }
  }

  /**
   * Validate pen.
   *
   * @param student             the student
   * @param isCreateOperation   the is create operation
   * @param apiValidationErrors the api validation errors
   */
  protected void validatePEN(BaseStudent student, boolean isCreateOperation, List<FieldError> apiValidationErrors) {
    Optional<StudentEntity> studentEntity = getStudentService().retrieveStudentByPen(student.getPen());
    if (isCreateOperation && studentEntity.isPresent()) {
      apiValidationErrors.add(createFieldError(PEN, student.getPen(), "PEN is already associated to a student."));
    } else if (studentEntity.isPresent() && !studentEntity.get().getStudentID().equals(UUID.fromString(student.getStudentID()))) {
      apiValidationErrors.add(createFieldError(PEN, student.getPen(), "Updated PEN number is already associated to a different student."));
    }
  }

  /**
   * Validate student history activity code.
   *
   * @param historyActivityCode the history activity code
   * @param apiValidationErrors the api validation errors
   */
  protected void validateStudentHistoryActivityCode(String historyActivityCode, List<FieldError> apiValidationErrors) {
    if (historyActivityCode != null) {
      Optional<StudentHistoryActivityCodeEntity> historyActivityCodeEntity = studentService.findStudentHistoryActivityCode(historyActivityCode);
      if (historyActivityCodeEntity.isEmpty()) {
        apiValidationErrors.add(createFieldError(HISTORY_ACTIVITY_CODE, historyActivityCode, "Invalid History Activity Code."));
      } else if (historyActivityCodeEntity.get().getEffectiveDate() != null && historyActivityCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(HISTORY_ACTIVITY_CODE, historyActivityCode, "History Activity Code provided is not yet effective."));
      } else if (historyActivityCodeEntity.get().getExpiryDate() != null && historyActivityCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
        apiValidationErrors.add(createFieldError(HISTORY_ACTIVITY_CODE, historyActivityCode, "History Activity Code provided has expired."));
      }
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("student", fieldName, rejectedValue, false, null, null, message);
  }
}
