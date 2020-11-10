package ca.bc.gov.educ.api.student.controller;

import ca.bc.gov.educ.api.student.endpoint.StudentTwinEndpoint;
import ca.bc.gov.educ.api.student.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.student.exception.errors.ApiError;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.mappers.StudentTwinMapper;
import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import ca.bc.gov.educ.api.student.service.StudentTwinService;
import ca.bc.gov.educ.api.student.struct.*;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import ca.bc.gov.educ.api.student.validator.StudentTwinPayloadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class StudentTwinController implements StudentTwinEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinService service;

  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinPayloadValidator payloadValidator;
  private static final StudentTwinMapper mapper = StudentTwinMapper.mapper;
  private static final StudentMapper studentMapper = StudentMapper.mapper;

  @Autowired
  StudentTwinController(final StudentTwinService studentTwinService, StudentTwinPayloadValidator payloadValidator) {
    this.service = studentTwinService;
    this.payloadValidator = payloadValidator;
  }

  /**
   * As the system stores only one mapping record between Student 1 and Student 2 during create.
   * while search STUDENT_ID for which twins record are being fetched could be in STUDENT_ID or TWINNED_STUDENT_ID column.
   * once all the records are retrieved go through the list and update the twin association based on direction.
   * A-> B or B-> A i.e Student 1 -> Student 2 or Student 2 -> student 1.
   *
   * @param studentID the id for which twin records will be fetched.
   * @return {@link StudentTwin} List.
   */
  public List<StudentTwin> findStudentTwins(String studentID) {
    val requestStudentID = UUID.fromString(studentID);
    val studentTwins = new ArrayList<StudentTwin>();
    val twinRecords = getService().findStudentTwins(requestStudentID);
    if (!CollectionUtils.isEmpty(twinRecords)) {
      for (val twinRecord : twinRecords) {
        final UUID twinStudentID;
        if (twinRecord.getStudentID().equals(requestStudentID)) {
          twinStudentID = twinRecord.getTwinStudentID();
        } else {
          twinStudentID = twinRecord.getStudentID();
        }
        studentTwins.add(createTwinRecord(studentID, twinRecord, twinStudentID));
      }
    }
    return studentTwins;
  }

  private StudentTwin createTwinRecord(String studentID, StudentTwinEntity twinRecord, UUID twinStudentID) {
    return StudentTwin.builder()
        .studentTwinID(twinRecord.getStudentTwinID().toString())
        .studentID(studentID)
        .createDate(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(twinRecord.getCreateDate()))
        .updateDate(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(twinRecord.getUpdateDate()))
        .createUser(twinRecord.getCreateUser())
        .updateUser(twinRecord.getUpdateUser())
        .twinStudentID(twinStudentID.toString())
        .studentTwinReasonCode(twinRecord.getStudentTwinReasonCode())
        .twinStudent(studentMapper.toStructure(getService().findStudentByID(twinStudentID)))
        .build();
  }

  public StudentTwin createStudentTwin(String studentID, StudentTwin studentTwin) {
    RequestUtil.setAuditColumnsForCreate(studentTwin);
    StudentTwinEntity entity = mapper.toModel(studentTwin);
    validatePayload(studentID, studentTwin, true);
    val twinEntity = getService().createStudentTwin(entity);
    StudentTwin studentTwinStruct = mapper.toStructure(twinEntity);
    studentTwinStruct.setTwinStudent(studentMapper.toStructure(getService().findStudentByID(twinEntity.getTwinStudentID())));
    return studentTwinStruct;
  }

  public List<StudentTwinReasonCode> getStudentTwinReasonCodes() {
    return getService().getStudentTwinReasonCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  private void validatePayload(String studentID, StudentTwin studentTwin, boolean isCreateOperation) {
    val validationResult = getPayloadValidator().validatePayload(studentID, studentTwin, isCreateOperation);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

  @Override
  @Transactional
  public ResponseEntity<Void> deleteById(final UUID studentID, final UUID studentTwinID) {
    getService().deleteById(studentTwinID);
    return ResponseEntity.noContent().build();
  }

}
