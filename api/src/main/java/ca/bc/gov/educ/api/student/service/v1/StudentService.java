package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.constant.EventOutcome;
import ca.bc.gov.educ.api.student.constant.EventType;
import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.*;
import ca.bc.gov.educ.api.student.repository.v1.StudentEventRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentRepository;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.struct.v1.StudentUpdate;
import ca.bc.gov.educ.api.student.util.JsonUtil;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.api.student.constant.EventOutcome.STUDENT_CREATED;
import static ca.bc.gov.educ.api.student.constant.EventOutcome.STUDENT_UPDATED;
import static ca.bc.gov.educ.api.student.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.student.constant.EventType.CREATE_STUDENT;
import static ca.bc.gov.educ.api.student.constant.EventType.UPDATE_STUDENT;
import static lombok.AccessLevel.PRIVATE;

/**
 * StudentService
 *
 * @author John Cox
 */
@Service
@Slf4j
public class StudentService {
  private static final String STUDENT_ID_ATTRIBUTE = "studentID";
  private final Executor paginatedQueryExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async-pagination-query-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
  @Getter(PRIVATE)
  private final StudentEventRepository studentEventRepository;

  @Getter(AccessLevel.PRIVATE)
  private final StudentRepository repository;

  @Getter(AccessLevel.PRIVATE)
  private final StudentHistoryService studentHistoryService;


  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  /**
   * Instantiates a new Student service.
   *
   * @param studentEventRepository the student event repository
   * @param repository             the repository
   * @param codeTableService       the code table service
   * @param studentHistoryService  the student history service
   */
  @Autowired
  public StudentService(StudentEventRepository studentEventRepository, final StudentRepository repository,
                        CodeTableService codeTableService, StudentHistoryService studentHistoryService) {
    this.studentEventRepository = studentEventRepository;
    this.repository = repository;
    this.codeTableService = codeTableService;
    this.studentHistoryService = studentHistoryService;
  }

  /**
   * Search for StudentEntity by id
   *
   * @param studentID the unique GUID for a given student.
   * @return the Student entity if found.
   * @throws EntityNotFoundException if the entity is not found in the  database by its GUID.
   */
  public StudentEntity retrieveStudent(UUID studentID) {
    Optional<StudentEntity> result = repository.findById(studentID);
    if (result.isPresent()) {
      return result.get();
    } else {
      throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, studentID.toString());
    }
  }

  /**
   * Search for StudentEntity by PEN
   *
   * @param pen the unique PEN for a given student.
   * @return the Student entity if found.
   */
  public Optional<StudentEntity> retrieveStudentByPen(String pen) {
    return repository.findStudentEntityByPen(pen);
  }

  /**
   * Creates a StudentEntity
   *
   * @param studentCreate the payload which will create the student record.
   * @return the saved instance.
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public Pair<StudentEntity, StudentEvent> createStudent(StudentCreate studentCreate) throws JsonProcessingException {
    var student = StudentMapper.mapper.toModel(studentCreate);
    TransformUtil.uppercaseFields(student);
    repository.save(student);
    studentHistoryService.createStudentHistory(student, studentCreate.getHistoryActivityCode(), student.getCreateUser(), false);
    final StudentEvent studentEvent =
      createStudentEvent(studentCreate.getCreateUser(), studentCreate.getUpdateUser(), JsonUtil.getJsonStringFromObject(StudentMapper.mapper.toStructure(student, studentCreate.getHistoryActivityCode())), CREATE_STUDENT, STUDENT_CREATED);
    getStudentEventRepository().save(studentEvent);
    return Pair.of(student, studentEvent);
  }

  private StudentEvent createStudentEvent(String createUser, String updateUser, String jsonString, EventType eventType, EventOutcome eventOutcome) {
    return StudentEvent.builder()
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .createUser(createUser)
      .updateUser(updateUser)
      .eventPayload(jsonString)
      .eventType(eventType.toString())
      .eventStatus(DB_COMMITTED.toString())
      .eventOutcome(eventOutcome.toString())
      .build();
  }

  /**
   * Updates a StudentEntity
   *
   * @param studentUpdate the payload which will update the DB record for the given student.
   * @param studentID     the student id
   * @return the updated entity.
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = {EntityNotFoundException.class})
  public Pair<StudentEntity, StudentEvent> updateStudent(StudentUpdate studentUpdate, UUID studentID) throws JsonProcessingException {

    var student = StudentMapper.mapper.toModel(studentUpdate);
    if (studentID == null || !studentID.equals(student.getStudentID())) {
      throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, String.valueOf(studentID));
    }
    Optional<StudentEntity> curStudentEntityOptional = repository.findById(student.getStudentID());

    if (curStudentEntityOptional.isPresent()) {
      final StudentEntity currentStudentEntity = curStudentEntityOptional.get();
      BeanUtils.copyProperties(student, currentStudentEntity, "createDate", "createUser"); // update current student entity with incoming payload ignoring the fields.
      TransformUtil.uppercaseFields(currentStudentEntity); // convert the input to upper case.
      studentHistoryService.createStudentHistory(currentStudentEntity, studentUpdate.getHistoryActivityCode(), currentStudentEntity.getUpdateUser(), false);
      final StudentEvent studentEvent =
        createStudentEvent(studentUpdate.getUpdateUser(), studentUpdate.getUpdateUser(), JsonUtil.getJsonStringFromObject(StudentMapper.mapper.toStudentUpdateStruct(currentStudentEntity, studentUpdate.getHistoryActivityCode())), UPDATE_STUDENT, STUDENT_UPDATED);
      repository.save(currentStudentEntity);
      getStudentEventRepository().save(studentEvent);
      return Pair.of(currentStudentEntity, studentEvent);
    } else {
      throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, student.getStudentID().toString());
    }
  }

  /**
   * Delete by id.
   *
   * @param id the id
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteById(UUID id) {
    val entityOptional = getRepository().findById(id);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, id.toString()));
    getStudentHistoryService().deleteByStudentID(id);
    getRepository().delete(entity);
  }

  /**
   * Find all completable future.
   *
   * @param studentSpecs the student specs
   * @param pageNumber   the page number
   * @param pageSize     the page size
   * @param sorts        the sorts
   * @return the completable future
   */
  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<StudentEntity>> findAll(Specification<StudentEntity> studentSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    return CompletableFuture.supplyAsync(() -> {
      Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
      try {
        return getRepository().findAll(studentSpecs, paging);
      } catch (final Exception ex) {
        throw new CompletionException(ex);
      }
    }, paginatedQueryExecutor);

  }

  /**
   * Gets gender codes list.
   *
   * @return the gender codes list
   */
  public List<GenderCodeEntity> getGenderCodesList() {
    return getCodeTableService().getGenderCodesList();
  }

  /**
   * Gets sex codes list.
   *
   * @return the sex codes list
   */
  public List<SexCodeEntity> getSexCodesList() {
    return getCodeTableService().getSexCodesList();
  }

  /**
   * Gets demog codes list.
   *
   * @return the demog codes list
   */
  public List<DemogCodeEntity> getDemogCodesList() {
    return getCodeTableService().getDemogCodesList();
  }

  /**
   * Gets grade codes list.
   *
   * @return the grade codes list
   */
  public List<GradeCodeEntity> getGradeCodesList() {
    return getCodeTableService().getGradeCodesList();
  }

  /**
   * Gets status codes list.
   *
   * @return the status codes list
   */
  public List<StatusCodeEntity> getStatusCodesList() {
    return getCodeTableService().getStatusCodesList();
  }

  /**
   * Find gender code optional.
   *
   * @param genderCode the gender code
   * @return the optional
   */
  public Optional<GenderCodeEntity> findGenderCode(String genderCode) {
    return getCodeTableService().findGenderCode(genderCode);
  }

  /**
   * Find sex code optional.
   *
   * @param sexCode the sex code
   * @return the optional
   */
  public Optional<SexCodeEntity> findSexCode(String sexCode) {
    return getCodeTableService().findSexCode(sexCode);
  }

  /**
   * Find student history activity code optional.
   *
   * @param historyActivityCode the history activity code
   * @return the optional
   */
  public Optional<StudentHistoryActivityCodeEntity> findStudentHistoryActivityCode(String historyActivityCode) {
    return getCodeTableService().findStudentHistoryActivityCode(historyActivityCode);
  }

  /**
   * Find Document Type code optional.
   *
   * @param documentTypeCode the Document Type code
   * @return the optional
   */
  public Optional<DocumentTypeCodeEntity> findDocTypeCode(String documentTypeCode) {
    return getCodeTableService().findDocumentTypeCode(documentTypeCode);
  }

  public List<DocumentTypeCodeEntity> getAllDocTypeCodes(){
    return getCodeTableService().getDocumentTypeCodes();
  }
}
