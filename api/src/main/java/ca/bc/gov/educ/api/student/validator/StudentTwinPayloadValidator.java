package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;

import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import ca.bc.gov.educ.api.student.model.StudentTwinReasonCodeEntity;
import ca.bc.gov.educ.api.student.service.StudentTwinService;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.StudentTwin;
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

@Component
public class StudentTwinPayloadValidator {

  public static final String MERGE_DIRECTION_CODE = "studentMergeDirectionCode";
  public static final String MERGE_SOURCE_CODE = "studentMergeSourceCode";
  public static final String STUDENT_ID = "studentID";
  public static final String MERGE_STUDENT_ID = "mergeStudentID";
  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinService studentTwinService;

  @Getter(AccessLevel.PRIVATE)
  private final StudentService studentService;

  @Autowired
  public StudentTwinPayloadValidator(final StudentTwinService studentTwinService, final StudentService studentService) {
    this.studentTwinService = studentTwinService;
    this.studentService = studentService;
  }

  public List<FieldError> validatePayload(String studentID, StudentTwin studentTwin, boolean isCreateOperation, StudentTwinEntity studentTwinEntity) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && studentTwin.getStudentTwinID() != null) {
      apiValidationErrors.add(createFieldError("studentTwinID", studentTwin.getStudentTwinID(), "studentTwinID should be null for post operation."));
    }
    validateStudentID(studentID, studentTwin, apiValidationErrors);
    validateTwinStudentID(studentTwin, apiValidationErrors, studentTwinEntity);
    validateTwinReasonCode(studentTwin, apiValidationErrors);
    return apiValidationErrors;
  }

  protected void validateTwinReasonCode(StudentTwin studentTwin, List<FieldError> apiValidationErrors) {
	  if(studentTwin.getStudentTwinReasonCode() != null) {
	    Optional<StudentTwinReasonCodeEntity> twinReasonCodeEntity = studentTwinService.findStudentTwinReasonCode(studentTwin.getStudentTwinReasonCode());
	   	if (!twinReasonCodeEntity.isPresent()) {
	      apiValidationErrors.add(createFieldError(MERGE_SOURCE_CODE, studentTwin.getStudentTwinReasonCode(), "Invalid Student Twin Reason Code."));
	   	} else if (twinReasonCodeEntity.get().getEffectiveDate() != null && twinReasonCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
	      apiValidationErrors.add(createFieldError(MERGE_SOURCE_CODE, studentTwin.getStudentTwinReasonCode(), "Student Twin Reason Code provided is not yet effective."));
	    } else if (twinReasonCodeEntity.get().getExpiryDate() != null && twinReasonCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
	      apiValidationErrors.add(createFieldError(MERGE_SOURCE_CODE, studentTwin.getStudentTwinReasonCode(), "Student Twin Reason Code provided has expired."));
	    }
	  }
  }

  protected void validateStudentID(String studentID, StudentTwin studentTwin, List<FieldError> apiValidationErrors) {
    if (!studentID.equals(studentTwin.getStudentID())) {
      apiValidationErrors.add(createFieldError(STUDENT_ID, studentTwin.getStudentID(), "Student ID is not consistent in the request."));
      return;
    }
    try {
      getStudentService().retrieveStudent(UUID.fromString(studentID));
    } catch (EntityNotFoundException e) {
      apiValidationErrors.add(createFieldError(STUDENT_ID, studentID, "Student ID does not exist."));
    }
  }

  protected void validateTwinStudentID(StudentTwin studentTwin, List<FieldError> apiValidationErrors, StudentTwinEntity studentTwinEntity) {
    try {
      var twinStudent = getStudentService().retrieveStudent(UUID.fromString(studentTwin.getTwinStudentID()));
      studentTwinEntity.setTwinStudent(twinStudent);
    } catch (EntityNotFoundException e) {
      apiValidationErrors.add(createFieldError(MERGE_STUDENT_ID, studentTwin.getTwinStudentID(), "Twin Student ID does not exist."));
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("studentTwin", fieldName, rejectedValue, false, null, null, message);
  }

}
