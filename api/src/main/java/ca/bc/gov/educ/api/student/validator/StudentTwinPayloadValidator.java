package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.model.StudentTwinReasonCodeEntity;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.service.StudentTwinService;
import ca.bc.gov.educ.api.student.struct.StudentTwin;
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
public class StudentTwinPayloadValidator extends BasePayloadValidator {

  public static final String TWIN_REASON_CODE = "studentTwinReasonCode";
  public static final String STUDENT_ID = "studentID";
  public static final String TWIN_STUDENT_ID = "twinStudentID";
  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinService studentTwinService;

  @Getter(AccessLevel.PRIVATE)
  private final StudentService studentService;

  @Autowired
  public StudentTwinPayloadValidator(final StudentTwinService studentTwinService, final StudentService studentService) {
    this.studentTwinService = studentTwinService;
    this.studentService = studentService;
  }

  public List<FieldError> validatePayload(String studentID, StudentTwin studentTwin, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && studentTwin.getStudentTwinID() != null) {
      apiValidationErrors.add(createFieldError(STUDENT_TWIN, "studentTwinID", studentTwin.getStudentTwinID(), "studentTwinID should be null for post operation."));
    }
    validateStudentID(studentID, studentTwin, apiValidationErrors);
    validateTwinStudentID(studentTwin, apiValidationErrors);
    validateTwinReasonCode(studentTwin, apiValidationErrors);
    return apiValidationErrors;
  }

  protected void validateTwinReasonCode(StudentTwin studentTwin, List<FieldError> apiValidationErrors) {
    if (studentTwin.getStudentTwinReasonCode() != null) {
      Optional<StudentTwinReasonCodeEntity> twinReasonCodeEntity = studentTwinService.findStudentTwinReasonCode(studentTwin.getStudentTwinReasonCode());
      validateTwinReasonCodeAgainstDB(studentTwin.getStudentTwinReasonCode(), apiValidationErrors, twinReasonCodeEntity);
    }
  }



  protected void validateStudentID(String studentID, StudentTwin studentTwin, List<FieldError> apiValidationErrors) {
    if (!studentID.equals(studentTwin.getStudentID())) {
      apiValidationErrors.add(createFieldError(STUDENT_TWIN, STUDENT_ID, studentTwin.getStudentID(), "Student ID is not consistent in the request."));
      return;
    }
    try {
      getStudentService().retrieveStudent(UUID.fromString(studentID));
    } catch (EntityNotFoundException e) {
      apiValidationErrors.add(createFieldError(STUDENT_TWIN, STUDENT_ID, studentID, "Student ID does not exist."));
    }
  }

  protected void validateTwinStudentID(StudentTwin studentTwin, List<FieldError> apiValidationErrors) {
    try {
      getStudentService().retrieveStudent(UUID.fromString(studentTwin.getTwinStudentID()));
    } catch (EntityNotFoundException e) {
      apiValidationErrors.add(createFieldError(STUDENT_TWIN, TWIN_STUDENT_ID, studentTwin.getTwinStudentID(), "Twin Student ID does not exist."));
    }
  }



}
