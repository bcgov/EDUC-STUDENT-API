package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.endpoint.StudentEndpoint;
import ca.bc.gov.educ.api.student.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.student.exception.errors.ApiError;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.service.StudentService;
import ca.bc.gov.educ.api.student.struct.Student;
import ca.bc.gov.educ.api.student.validator.StudentPayloadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Student controller
 *
 * @author John Cox
 */

@RestController
@EnableResourceServer
@Slf4j
public class StudentController implements StudentEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final StudentService service;

  @Getter(AccessLevel.PRIVATE)
  private final StudentPayloadValidator payloadValidator;
  private static final StudentMapper mapper = StudentMapper.mapper;

  @Autowired
  StudentController(final StudentService studentService, StudentPayloadValidator payloadValidator) {
    this.service = studentService;
    this.payloadValidator = payloadValidator;
  }

  public Student readStudent(String studentID) {
    return mapper.toStructure(getService().retrieveStudent(UUID.fromString(studentID)));
  }
  
  public Iterable<Student> findStudent(String pen) {
	  Optional<StudentEntity> studentsResponse = getService().retrieveStudentByPen(pen);
	  return studentsResponse.map(mapper::toStructure).map(Collections::singletonList).orElseGet(Collections::emptyList);
  }

  public Student createStudent(Student student) {
    val validationResult = getPayloadValidator().validatePayload(student, true);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
    setAuditColumns(student);
    return mapper.toStructure(getService().createStudent(mapper.toModel(student)));
  }

  public Student updateStudent(Student student) {
    val validationResult = getPayloadValidator().validatePayload(student, false);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
    setAuditColumns(student);
    return mapper.toStructure(getService().updateStudent(mapper.toModel(student)));
  }

  @Override
  public String health() {
    return "OK";
  }

  /**
   * set audit data to the object.
   *
   * @param student The object which will be persisted.
   */
  private void setAuditColumns(Student student) {
    if (StringUtils.isBlank(student.getCreateUser())) {
      student.setCreateUser(ApplicationProperties.STUDENT_API);
    }
    if (StringUtils.isBlank(student.getUpdateUser())) {
      student.setUpdateUser(ApplicationProperties.STUDENT_API);
    }
    student.setCreateDate(LocalDateTime.now().toString());
    student.setUpdateDate(LocalDateTime.now().toString());
  }

}
