package ca.bc.gov.educ.api.student.controller.v1;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.endpoint.v1.StudentEndpoint;
import ca.bc.gov.educ.api.student.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.student.exception.errors.ApiError;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.messaging.stan.Publisher;
import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentEvent;
import ca.bc.gov.educ.api.student.service.v1.StudentEventService;
import ca.bc.gov.educ.api.student.service.v1.StudentSearchService;
import ca.bc.gov.educ.api.student.service.v1.StudentService;
import ca.bc.gov.educ.api.student.struct.v1.*;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import ca.bc.gov.educ.api.student.util.RequestUtil;
import ca.bc.gov.educ.api.student.validator.StudentPayloadValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.student.constant.Topics.STUDENT_EVENTS_TOPIC;
import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Student controller
 *
 * @author John Cox
 */
@RestController
@Slf4j
public class StudentController implements StudentEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final StudentService service;

  @Getter(AccessLevel.PRIVATE)
  private final Publisher publisher;

  @Getter(AccessLevel.PRIVATE)
  private final StudentEventService studentEventService;

  @Getter(AccessLevel.PRIVATE)
  private final StudentPayloadValidator payloadValidator;
  private static final StudentMapper mapper = StudentMapper.mapper;
  private final StudentSearchService studentSearchService;

  /**
   * Instantiates a new Student controller.
   *
   * @param studentService       the student service
   * @param publisher            the publisher
   * @param studentEventService  the student event service
   * @param payloadValidator     the payload validator
   * @param studentSearchService the student search service
   */
  @Autowired
  StudentController(final StudentService studentService, Publisher publisher, StudentEventService studentEventService, StudentPayloadValidator payloadValidator, StudentSearchService studentSearchService) {
    this.service = studentService;
    this.publisher = publisher;
    this.studentEventService = studentEventService;
    this.payloadValidator = payloadValidator;
    this.studentSearchService = studentSearchService;
  }

  public Student readStudent(String studentID) {
    return mapper.toStructure(getService().retrieveStudent(UUID.fromString(studentID)));
  }

  public List<Student> findStudent(String pen) {
    Optional<StudentEntity> studentsResponse = getService().retrieveStudentByPen(pen);
    return studentsResponse.map(mapper::toStructure).map(Collections::singletonList).orElseGet(Collections::emptyList);
  }

  public Student createStudent(StudentCreate student) throws JsonProcessingException {
    validatePayload(() -> getPayloadValidator().validateCreatePayload(student));
    RequestUtil.setAuditColumnsForCreate(student);
    var studentEventPair = getStudentEventService().createStudent(student);
    var eventToPublish = createEventToPublish(studentEventPair.getRight());
    publisher.dispatchMessage(STUDENT_EVENTS_TOPIC.toString(), eventToPublish);
    return mapper.toStructure(studentEventPair.getLeft());
  }

  @Override
  public Student updateStudent(UUID id, StudentUpdate student) throws JsonProcessingException {
    validatePayload(() -> getPayloadValidator().validateUpdatePayload(student));
    RequestUtil.setAuditColumnsForUpdate(student);
    var pair = getStudentEventService().updateStudent(student, id);
    var eventToPublish = createEventToPublish(pair.getRight());
    publisher.dispatchMessage(STUDENT_EVENTS_TOPIC.toString(), eventToPublish);
    return mapper.toStructure(pair.getLeft());
  }

  private void validatePayload(Supplier<List<FieldError>> validator) {
    val validationResult = validator.get();
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

  public List<GenderCode> getGenderCodes() {
    return getService().getGenderCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public List<SexCode> getSexCodes() {
    return getService().getSexCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public List<DemogCode> getDemogCodes() {
    return getService().getDemogCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public List<GradeCode> getGradeCodes() {
    return getService().getGradeCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public List<StatusCode> getStatusCodes() {
    return getService().getStatusCodesList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ResponseEntity<Void> deleteById(final UUID id) {
    getService().deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<Student>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<Sort.Order> sorts = new ArrayList<>();
    Specification<StudentEntity> studentSpecs = studentSearchService.setSpecificationAndSortCriteria(sortCriteriaJson, searchCriteriaListJson, objectMapper, sorts);
    return getService().findAll(studentSpecs, pageNumber, pageSize, sorts).thenApplyAsync(studentEntities -> studentEntities.map(mapper::toStructure));
  }

  private byte[] createEventToPublish(StudentEvent event) throws JsonProcessingException {
    ChoreographedEvent choreographedEvent = new ChoreographedEvent();
    choreographedEvent.setEventType(EventType.valueOf(event.getEventType()));
    choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome()));
    choreographedEvent.setEventPayload(event.getEventPayload());
    choreographedEvent.setEventID(event.getEventId().toString());
    return JsonUtil.getJsonBytesFromObject(choreographedEvent);
  }

}
