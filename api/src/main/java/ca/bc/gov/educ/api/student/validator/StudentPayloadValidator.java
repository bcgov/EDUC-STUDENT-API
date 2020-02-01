package ca.bc.gov.educ.api.student.validator;

import ca.bc.gov.educ.api.student.exception.errors.ApiSubError;
import ca.bc.gov.educ.api.student.exception.errors.ApiValidationError;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.service.CodeTableService;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.Student;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StudentPayloadValidator {

  @Getter(AccessLevel.PRIVATE)
  private final StudentService studentService;
  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  @Autowired
  public StudentPayloadValidator(final StudentService studentService, final CodeTableService codeTableService) {
    this.studentService = studentService;
    this.codeTableService = codeTableService;
  }

  public List<FieldError> validatePayload(Student student, boolean isCreateOperation) {
    List<FieldError> apiValidationErrors = new ArrayList<>();
    Optional<StudentEntity> studentEntity = getStudentService().retrieveStudentByPen(student.getPen());
    if (isCreateOperation && studentEntity.isPresent()) {
      apiValidationErrors.add(new FieldError("student", "pen", student.getPen(), false, null, null, "PEN is already associated to a student."));
    } else if (studentEntity.isPresent() && !studentEntity.get().getStudentID().equals(UUID.fromString(student.getStudentID()))) {
      apiValidationErrors.add(new FieldError("student", "pen", student.getPen(), false, null, null, "Updated PEN number is already associated to a different student."));
    }
    if (codeTableService.findDataSourceCode(student.getDataSourceCode()) == null) {
      apiValidationErrors.add(new FieldError("student", "dataSourceCode", student.getDataSourceCode(), false, null, null, "Invalid Data Source Code."));
    }
    if (codeTableService.findGenderCode(student.getGenderCode()) == null) {
      apiValidationErrors.add(new FieldError("student", "genderCode", student.getGenderCode(), false, null, null, "Invalid Gender Code."));
    }
    if (StringUtils.isNotBlank(student.getEmail())) {
      Optional<StudentEntity> studentEntityByEmail = getStudentService().retrieveStudentByEmail(student.getEmail());
      if (isCreateOperation && studentEntityByEmail.isPresent()) {
        apiValidationErrors.add(new FieldError("student", "pen", student.getEmail(), false, null, null, "Email is already associated to a student."));
      } else if (studentEntityByEmail.isPresent() && !studentEntityByEmail.get().getStudentID().equals(UUID.fromString(student.getStudentID()))) {
        apiValidationErrors.add(new FieldError("student", "pen", student.getEmail(), false, null, null, "Updated Email is already associated to a different student."));
      }
    }
    return apiValidationErrors;
  }
}
