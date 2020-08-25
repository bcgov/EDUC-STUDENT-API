package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.endpoint.StudentTwinEndpoint;
import ca.bc.gov.educ.api.student.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.student.exception.errors.ApiError;
import ca.bc.gov.educ.api.student.mappers.StudentTwinMapper;
import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import ca.bc.gov.educ.api.student.service.StudentTwinService;
import ca.bc.gov.educ.api.student.struct.*;
import ca.bc.gov.educ.api.student.validator.StudentTwinPayloadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Student Merge controller
 *
 * @author Mingwei
 */

@RestController
@EnableResourceServer
@Slf4j
public class StudentTwinController extends BaseController implements StudentTwinEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinService service;

  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinPayloadValidator payloadValidator;
  private static final StudentTwinMapper mapper = StudentTwinMapper.mapper;

  @Autowired
  StudentTwinController(final StudentTwinService studentTwinService, StudentTwinPayloadValidator payloadValidator) {
    this.service = studentTwinService;
    this.payloadValidator = payloadValidator;
  }

  public List<StudentTwin> findStudentTwins(String studentID) {
    return getService().findStudentTwins(UUID.fromString(studentID)).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public StudentTwin createStudentTwin(String studentID, StudentTwin studentTwin) {
    setAuditColumns(studentTwin);
    StudentTwinEntity entity = mapper.toModel(studentTwin);
    validatePayload(studentID, studentTwin, true, entity);
    return mapper.toStructure(getService().createStudentTwin(entity));
  }

  public List<StudentTwinReasonCode> getStudentTwinReasonCodes() {
    return getService().getStudentTwinReasonCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  private void validatePayload(String studentID, StudentTwin studentTwin, boolean isCreateOperation, StudentTwinEntity studentTwinEntity) {
    val validationResult = getPayloadValidator().validatePayload(studentID, studentTwin, isCreateOperation, studentTwinEntity);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

}
