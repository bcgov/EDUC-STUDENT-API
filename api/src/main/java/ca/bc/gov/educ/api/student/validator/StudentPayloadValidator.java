package ca.bc.gov.educ.api.student.validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import ca.bc.gov.educ.api.student.model.GenderCodeEntity;
import ca.bc.gov.educ.api.student.model.SexCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.Student;
import lombok.AccessLevel;
import lombok.Getter;

@Component
public class StudentPayloadValidator {

  public static final String GENDER_CODE = "genderCode";
  public static final String SEX_CODE = "sexCode";
  public static final String PEN = "pen";
  @Getter(AccessLevel.PRIVATE)
  private final StudentService studentService;

  @Autowired
  public StudentPayloadValidator(final StudentService studentService) {
    this.studentService = studentService;
  }

  public List<FieldError> validatePayload(Student student, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && student.getStudentID() != null) {
      apiValidationErrors.add(createFieldError("studentID", student.getStudentID(), "studentID should be null for post operation."));
    }
    validatePEN(student, isCreateOperation, apiValidationErrors);
    validateGenderCode(student, apiValidationErrors);
    validateSexCode(student, apiValidationErrors);
    validateEmail(student, isCreateOperation, apiValidationErrors);
    return apiValidationErrors;
  }

  protected void validateGenderCode(Student student, List<FieldError> apiValidationErrors) {
	if(student.getGenderCode() != null) {
	    Optional<GenderCodeEntity> genderCodeEntity = studentService.findGenderCode(student.getGenderCode());
	   	if (!genderCodeEntity.isPresent()) {
	      apiValidationErrors.add(createFieldError(GENDER_CODE, student.getGenderCode(), "Invalid Gender Code."));
	   	} else if (genderCodeEntity.get().getEffectiveDate() != null && genderCodeEntity.get().getEffectiveDate().isAfter(LocalDateTime.now())) {
	      apiValidationErrors.add(createFieldError(GENDER_CODE, student.getGenderCode(), "Gender Code provided is not yet effective."));
	    } else if (genderCodeEntity.get().getExpiryDate() != null && genderCodeEntity.get().getExpiryDate().isBefore(LocalDateTime.now())) {
	      apiValidationErrors.add(createFieldError(GENDER_CODE, student.getGenderCode(), "Gender Code provided has expired."));
	    }
	}
  }
  
  protected void validateSexCode(Student student, List<FieldError> apiValidationErrors) {
	if(student.getSexCode() != null) {
	  Optional<SexCodeEntity> sexCodeEntity = studentService.findSexCode(student.getSexCode());
	  if (!sexCodeEntity.isPresent()) {
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

  protected void validateEmail(Student student, boolean isCreateOperation, List<FieldError> apiValidationErrors) {
    if (StringUtils.isNotBlank(student.getEmail())) {
      Optional<StudentEntity> studentEntityByEmail = getStudentService().retrieveStudentByEmail(student.getEmail());
      if (isCreateOperation && studentEntityByEmail.isPresent()) {
        apiValidationErrors.add(createFieldError(PEN, student.getEmail(), "Email is already associated to a student."));
      } else if (studentEntityByEmail.isPresent() && !studentEntityByEmail.get().getStudentID().equals(UUID.fromString(student.getStudentID()))) {
        apiValidationErrors.add(createFieldError(PEN, student.getEmail(), "Updated Email is already associated to a different student."));
      }
    }
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("student", fieldName, rejectedValue, false, null, null, message);
  }

}
