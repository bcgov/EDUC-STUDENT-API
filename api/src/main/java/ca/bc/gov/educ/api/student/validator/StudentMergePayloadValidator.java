package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentMergeEntity;
import ca.bc.gov.educ.api.student.model.StudentMergeSourceCodeEntity;
import ca.bc.gov.educ.api.student.service.StudentMergeService;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.StudentMerge;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StudentMergePayloadValidator extends BasePayloadValidator{

  public static final String MERGE_DIRECTION_CODE = "studentMergeDirectionCode";
  public static final String MERGE_SOURCE_CODE = "studentMergeSourceCode";
  public static final String STUDENT_ID = "studentID";
  public static final String MERGE_STUDENT_ID = "mergeStudentID";
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeService studentMergeService;

  @Getter(AccessLevel.PRIVATE)
  private final StudentService studentService;

  @Autowired
  public StudentMergePayloadValidator(final StudentMergeService studentMergeService, final StudentService studentService) {
    this.studentMergeService = studentMergeService;
    this.studentService = studentService;
  }

  public List<FieldError> validatePayload(String studentID, StudentMerge studentMerge, boolean isCreateOperation, StudentMergeEntity studentMergeEntity) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && studentMerge.getStudentMergeID() != null) {
      apiValidationErrors.add(createFieldError("studentMergeID", studentMerge.getStudentMergeID(), "studentMergeID should be null for post operation."));
    }
    validateStudentID(studentID, studentMerge, apiValidationErrors);
    validateMergeStudentID(studentMerge, apiValidationErrors, studentMergeEntity);
    validateMergeDirectionCode(studentMerge, apiValidationErrors);
    validateMergeSourceCode(studentMerge, apiValidationErrors);
    return apiValidationErrors;
  }

  protected void validateMergeDirectionCode(StudentMerge studentMerge, List<FieldError> apiValidationErrors) {
	  if(studentMerge.getStudentMergeDirectionCode() != null) {
	    Optional<StudentMergeDirectionCodeEntity> mergeDirectionCodeEntity = studentMergeService.findStudentMergeDirectionCode(studentMerge.getStudentMergeDirectionCode());
      validateMergeDirectionCodeAgainstDB(studentMerge.getStudentMergeDirectionCode(), apiValidationErrors, mergeDirectionCodeEntity);
    }
  }


  protected void validateMergeSourceCode(StudentMerge studentMerge, List<FieldError> apiValidationErrors) {
	  if(studentMerge.getStudentMergeSourceCode() != null) {
	    Optional<StudentMergeSourceCodeEntity> mergeSourceCodeEntity = studentMergeService.findStudentMergeSourceCode(studentMerge.getStudentMergeSourceCode());
      validateMergeSourceCodeAgainstDB(studentMerge.getStudentMergeSourceCode(), apiValidationErrors, mergeSourceCodeEntity);
    }
  }



  protected void validateStudentID(String studentID, StudentMerge studentMerge, List<FieldError> apiValidationErrors) {
    if (!studentID.equals(studentMerge.getStudentID())) {
      apiValidationErrors.add(createFieldError(STUDENT_ID, studentMerge.getStudentID(), "Student ID is not consistent in the request."));
      return;
    }
    try {
      getStudentService().retrieveStudent(UUID.fromString(studentID));
    } catch (EntityNotFoundException e) {
      apiValidationErrors.add(createFieldError(STUDENT_ID, studentID, "Student ID does not exist."));
    }
  }

  protected void validateMergeStudentID(StudentMerge studentMerge, List<FieldError> apiValidationErrors, StudentMergeEntity studentMergeEntity) {
    try {
      var mergeStudent = getStudentService().retrieveStudent(UUID.fromString(studentMerge.getMergeStudentID()));
      studentMergeEntity.setMergeStudent(mergeStudent);
    } catch (EntityNotFoundException e) {
      apiValidationErrors.add(createFieldError(MERGE_STUDENT_ID, studentMerge.getMergeStudentID(), "Merge Student ID does not exist."));
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError(STUDENT_MERGE, fieldName, rejectedValue, false, null, null, message);
  }

}
