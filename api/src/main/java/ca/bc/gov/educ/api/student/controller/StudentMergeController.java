package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.endpoint.StudentMergeEndpoint;
import ca.bc.gov.educ.api.student.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.student.exception.errors.ApiError;
import ca.bc.gov.educ.api.student.mappers.StudentMergeMapper;
import ca.bc.gov.educ.api.student.model.StudentMergeEntity;
import ca.bc.gov.educ.api.student.service.StudentMergeService;
import ca.bc.gov.educ.api.student.struct.StudentMerge;
import ca.bc.gov.educ.api.student.struct.StudentMergeSourceCode;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import ca.bc.gov.educ.api.student.validator.StudentMergePayloadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Student Merge controller
 *
 * @author Mingwei
 */

@RestController
@Slf4j
public class StudentMergeController implements StudentMergeEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeService service;

  @Getter(AccessLevel.PRIVATE)
  private final StudentMergePayloadValidator payloadValidator;
  private static final StudentMergeMapper mapper = StudentMergeMapper.mapper;

  @Autowired
  StudentMergeController(final StudentMergeService studentMergeService, StudentMergePayloadValidator payloadValidator) {
    this.service = studentMergeService;
    this.payloadValidator = payloadValidator;
  }

  public List<StudentMerge> findStudentMerges(String studentID, String mergeDirection) {
    return getService().findStudentMerges(UUID.fromString(studentID), mergeDirection).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public StudentMerge createStudentMerge(String studentID, StudentMerge studentMerge) {
    RequestUtil.setAuditColumnsForCreate(studentMerge);
    StudentMergeEntity entity = mapper.toModel(studentMerge);
    validatePayload(studentID, studentMerge, true, entity);
    return mapper.toStructure(getService().createStudentMerge(entity));
  }

  public List<StudentMergeSourceCode> getStudentMergeSourceCodes() {
    return getService().getStudentMergeSourceCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  private void validatePayload(String studentID, StudentMerge studentMerge, boolean isCreateOperation, StudentMergeEntity studentMergeEntity) {
    val validationResult = getPayloadValidator().validatePayload(studentID, studentMerge, isCreateOperation, studentMergeEntity);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

}
