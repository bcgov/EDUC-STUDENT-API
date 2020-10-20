package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentTwinReasonCodeEntity;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ca.bc.gov.educ.api.student.validator.StudentMergePayloadValidator.MERGE_DIRECTION_CODE;
import static ca.bc.gov.educ.api.student.validator.StudentMergePayloadValidator.MERGE_SOURCE_CODE;
import static ca.bc.gov.educ.api.student.validator.StudentTwinPayloadValidator.TWIN_REASON_CODE;

public abstract class BasePayloadValidator {

  public static final String STUDENT_TWIN = "studentTwin";
  public static final String STUDENT_MERGE = "studentMerge";

  protected void validateTwinReasonCodeAgainstDB(String twinReasonCode, List<FieldError> apiValidationErrors, Optional<StudentTwinReasonCodeEntity> twinReasonCodeEntity) {
    if (twinReasonCodeEntity.isEmpty()) {
      apiValidationErrors.add(createFieldError(STUDENT_TWIN, TWIN_REASON_CODE, twinReasonCode, "Invalid Student Twin Reason Code."));
    } else if (twinReasonCodeEntity.get().getEffectiveDate() != null && twinReasonCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_TWIN, TWIN_REASON_CODE, twinReasonCode, "Student Twin Reason Code provided is not yet effective."));
    } else if (twinReasonCodeEntity.get().getExpiryDate() != null && twinReasonCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_TWIN, TWIN_REASON_CODE, twinReasonCode, "Student Twin Reason Code provided has expired."));
    }
  }

  protected void validateMergeDirectionCodeAgainstDB(String mergeDirectionCode, List<FieldError> apiValidationErrors, Optional<StudentMergeDirectionCodeEntity> mergeDirectionCodeEntity) {
    if (mergeDirectionCodeEntity.isEmpty()) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Invalid Student Merge Direction Code."));
    } else if (mergeDirectionCodeEntity.get().getEffectiveDate() != null && mergeDirectionCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Student Merge Direction Code provided is not yet effective."));
    } else if (mergeDirectionCodeEntity.get().getExpiryDate() != null && mergeDirectionCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_DIRECTION_CODE, mergeDirectionCode, "Student Merge Direction Code provided has expired."));
    }
  }

  protected void validateMergeSourceCodeAgainstDB(String mergeSourceCode , List<FieldError> apiValidationErrors, Optional<StudentMergeSourceCodeEntity> mergeSourceCodeEntity) {
    if (mergeSourceCodeEntity.isEmpty()) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Invalid Student Merge Source Code."));
    } else if (mergeSourceCodeEntity.get().getEffectiveDate() != null && mergeSourceCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Student Merge Source Code provided is not yet effective."));
    } else if (mergeSourceCodeEntity.get().getExpiryDate() != null && mergeSourceCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
      apiValidationErrors.add(createFieldError(STUDENT_MERGE, MERGE_SOURCE_CODE, mergeSourceCode, "Student Merge Source Code provided has expired."));
    }
  }

  protected FieldError createFieldError(String objectName, String fieldName, Object rejectedValue, String message) {
    return new FieldError(objectName, fieldName, rejectedValue, false, null, null, message);
  }
}
