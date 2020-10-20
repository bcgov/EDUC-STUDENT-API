package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.service.StudentMergeService;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.service.StudentTwinService;
import ca.bc.gov.educ.api.student.struct.Student;
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

import static ca.bc.gov.educ.api.student.validator.StudentTwinPayloadValidator.TWIN_STUDENT_ID;

@Component
public class StudentPayloadValidator extends BasePayloadValidator {

  public static final String GENDER_CODE = "genderCode";
  public static final String SEX_CODE = "sexCode";
  public static final String PEN = "pen";
  @Getter(AccessLevel.PRIVATE)
  private final StudentService studentService;
  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinService studentTwinService;
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeService studentMergeService;

  @Autowired
  public StudentPayloadValidator(final StudentService studentService, StudentTwinService studentTwinService, StudentMergeService studentMergeService) {
    this.studentService = studentService;
    this.studentTwinService = studentTwinService;
    this.studentMergeService = studentMergeService;
  }

  public List<FieldError> validatePayload(Student student, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && student.getStudentID() != null) {
      apiValidationErrors.add(createFieldError("studentID", student.getStudentID(), "studentID should be null for post operation."));
    }
    validatePEN(student, isCreateOperation, apiValidationErrors);
    validateGenderCode(student, apiValidationErrors);
    validateSexCode(student, apiValidationErrors);
    validateMergesIfPresent(student, apiValidationErrors);
    validateTwinsIfPresent(student, apiValidationErrors);
    return apiValidationErrors;
  }

  private void validateTwinsIfPresent(Student student, List<FieldError> apiValidationErrors) {
    if (student.getStudentTwinAssociations() != null && !student.getStudentTwinAssociations().isEmpty()) {
      for (var studentTwin : student.getStudentTwinAssociations()) {
        Optional<StudentTwinReasonCodeEntity> twinReasonCodeEntity = studentTwinService.findStudentTwinReasonCode(studentTwin.getStudentTwinReasonCode());
        validateTwinReasonCodeAgainstDB(studentTwin.getStudentTwinReasonCode(), apiValidationErrors, twinReasonCodeEntity);
        try {
          getStudentService().retrieveStudent(UUID.fromString(studentTwin.getTwinStudentID()));
        } catch (final Exception ex) {
          apiValidationErrors.add(createFieldError("studentTwin", TWIN_STUDENT_ID, studentTwin.getTwinStudentID(), "Twin Student ID does not exist."));
        }
      }
    }
  }

  private void validateMergesIfPresent(Student student, List<FieldError> apiValidationErrors) {
    if (student.getStudentMergeAssociations() != null && !student.getStudentMergeAssociations().isEmpty()) {
      for (var studentMerge : student.getStudentMergeAssociations()) {
        Optional<StudentMergeDirectionCodeEntity> mergeDirectionCodeEntity = studentMergeService.findStudentMergeDirectionCode(studentMerge.getStudentMergeDirectionCode());
        Optional<StudentMergeSourceCodeEntity> mergeSourceCodeEntity = studentMergeService.findStudentMergeSourceCode(studentMerge.getStudentMergeSourceCode());
        validateMergeSourceCodeAgainstDB(studentMerge.getStudentMergeSourceCode(), apiValidationErrors, mergeSourceCodeEntity);
        validateMergeDirectionCodeAgainstDB(studentMerge.getStudentMergeDirectionCode(), apiValidationErrors, mergeDirectionCodeEntity);
      }
    }
  }

  protected void validateGenderCode(Student student, List<FieldError> apiValidationErrors) {
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

  protected void validateSexCode(Student student, List<FieldError> apiValidationErrors) {
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

  protected void validatePEN(Student student, boolean isCreateOperation, List<FieldError> apiValidationErrors) {
    Optional<StudentEntity> studentEntity = getStudentService().retrieveStudentByPen(student.getPen());
    if (isCreateOperation && studentEntity.isPresent()) {
      apiValidationErrors.add(createFieldError(PEN, student.getPen(), "PEN is already associated to a student."));
    } else if (studentEntity.isPresent() && !studentEntity.get().getStudentID().equals(UUID.fromString(student.getStudentID()))) {
      apiValidationErrors.add(createFieldError(PEN, student.getPen(), "Updated PEN number is already associated to a different student."));
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return super.createFieldError("student", fieldName, rejectedValue, message);
  }

}
