package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.mappers.v1.StudentMapper;
import ca.bc.gov.educ.api.student.model.v1.*;
import ca.bc.gov.educ.api.student.repository.v1.StudentMergeRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentRepository;
import ca.bc.gov.educ.api.student.repository.v1.StudentTwinRepository;
import ca.bc.gov.educ.api.student.struct.v1.StudentCreate;
import ca.bc.gov.educ.api.student.struct.v1.StudentUpdate;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * StudentService
 *
 * @author John Cox
 */
@Service
@Slf4j
public class StudentService {
  private final Executor paginatedQueryExecutor = Executors.newFixedThreadPool(10);
  private static final String STUDENT_ID_ATTRIBUTE = "studentID";

  @Getter(AccessLevel.PRIVATE)
  private final StudentRepository repository;

  @Getter(AccessLevel.PRIVATE)
  private final StudentHistoryService studentHistoryService;

  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeRepository studentMergeRepo;

  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinRepository studentTwinRepo;

  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  /**
   * Instantiates a new Student service.
   *
   * @param repository            the repository
   * @param studentMergeRepo      the student merge repo
   * @param studentTwinRepo       the student twin repo
   * @param codeTableService      the code table service
   * @param studentHistoryService the student history service
   */
  @Autowired
  public StudentService(final StudentRepository repository, StudentMergeRepository studentMergeRepo, StudentTwinRepository studentTwinRepo,
                        CodeTableService codeTableService, StudentHistoryService studentHistoryService) {
    this.repository = repository;
    this.studentMergeRepo = studentMergeRepo;
    this.studentTwinRepo = studentTwinRepo;
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
   * @throws InvalidParameterException if Student GUID is passed in the payload for the post operation it will throw this exception.
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public StudentEntity createStudent(StudentCreate studentCreate) {
    var student = StudentMapper.mapper.toModel(studentCreate);
    TransformUtil.uppercaseFields(student);
    repository.save(student);
    studentHistoryService.createStudentHistory(student, studentCreate.getHistoryActivityCode(), student.getCreateUser());
    return student;
  }

  /**
   * Updates a StudentEntity
   *
   * @param studentUpdate the payload which will update the DB record for the given student.
   * @param studentID     the student id
   * @return the updated entity.
   * @throws EntityNotFoundException if the entity does not exist in the DB.
   */
  @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = {EntityNotFoundException.class})
  public StudentEntity updateStudent(StudentUpdate studentUpdate, UUID studentID) {

    var student = StudentMapper.mapper.toModel(studentUpdate);
    if(studentID == null || !studentID.equals(student.getStudentID())){
      throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, String.valueOf(studentID));
    }
    Optional<StudentEntity> curStudentEntity = repository.findById(student.getStudentID());

    if (curStudentEntity.isPresent()) {
      final StudentEntity newStudentEntity = curStudentEntity.get();
      val createUser = newStudentEntity.getCreateUser();
      val createDate = newStudentEntity.getCreateDate();
      BeanUtils.copyProperties(student, newStudentEntity);
      newStudentEntity.setCreateUser(createUser);
      newStudentEntity.setCreateDate(createDate);
      TransformUtil.uppercaseFields(newStudentEntity);
      studentHistoryService.createStudentHistory(newStudentEntity, studentUpdate.getHistoryActivityCode(), newStudentEntity.getUpdateUser());
      return repository.save(newStudentEntity);
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
    var twins = getStudentTwinRepo().findByStudentIDOrTwinStudentID(entity.getStudentID(), entity.getStudentID());
    if (!twins.isEmpty()) {
      getStudentTwinRepo().deleteAll(twins);
    }
    var merges = getStudentMergeRepo().findStudentMergeEntityByStudentID(entity.getStudentID());
    var merges2 = getStudentMergeRepo().findStudentMergeEntityByMergeStudent(entity);
    if (!merges.isEmpty()) {
      getStudentMergeRepo().deleteAll(merges);
    }
    if (!merges2.isEmpty()) {
      getStudentMergeRepo().deleteAll(merges2);
    }
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
   * Create student with associations student entity.
   *
   * @param student the student
   * @return the student entity
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public StudentEntity createStudentWithAssociations(StudentCreate student) {
    StudentEntity studentEntity = StudentMapper.mapper.toModel(student);
    TransformUtil.uppercaseFields(studentEntity);
    getRepository().save(studentEntity);
    studentHistoryService.createStudentHistory(studentEntity, student.getHistoryActivityCode(), student.getCreateUser());
    if (!CollectionUtils.isEmpty(student.getStudentMergeAssociations())) {
      List<StudentMergeEntity> studentMergeEntities = new ArrayList<>();
      for (var mergeStudent : student.getStudentMergeAssociations()) {
        var studentMergeEntity = new StudentMergeEntity();
        studentMergeEntity.setCreateDate(studentEntity.getCreateDate());
        studentMergeEntity.setUpdateDate(studentEntity.getUpdateDate());
        studentMergeEntity.setCreateUser(studentEntity.getCreateUser());
        studentMergeEntity.setUpdateUser(studentEntity.getUpdateUser());
        studentMergeEntity.setStudentMergeDirectionCode(mergeStudent.getStudentMergeDirectionCode());
        studentMergeEntity.setStudentMergeSourceCode(mergeStudent.getStudentMergeSourceCode());
        studentMergeEntity.setMergeStudent(getRepository().findById(UUID.fromString(mergeStudent.getMergeStudentID())).orElseThrow());
        studentMergeEntity.setStudentID(studentEntity.getStudentID());
        studentMergeEntities.add(studentMergeEntity);
      }
      getStudentMergeRepo().saveAll(studentMergeEntities);
    }
    if (!CollectionUtils.isEmpty(student.getStudentTwinAssociations())) {
      List<StudentTwinEntity> studentTwinEntities = new ArrayList<>();
      for (var twinStudent : student.getStudentTwinAssociations()) {
        var studentTwinEntity = new StudentTwinEntity();
        studentTwinEntity.setCreateDate(studentEntity.getCreateDate());
        studentTwinEntity.setUpdateDate(studentEntity.getUpdateDate());
        studentTwinEntity.setCreateUser(studentEntity.getCreateUser());
        studentTwinEntity.setUpdateUser(studentEntity.getUpdateUser());
        studentTwinEntity.setStudentTwinReasonCode(twinStudent.getStudentTwinReasonCode());
        studentTwinEntity.setTwinStudentID(UUID.fromString(twinStudent.getTwinStudentID()));
        studentTwinEntity.setStudentID(studentEntity.getStudentID());
        studentTwinEntities.add(studentTwinEntity);
      }
      getStudentTwinRepo().saveAll(studentTwinEntities);
    }

    return studentEntity;
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
}
