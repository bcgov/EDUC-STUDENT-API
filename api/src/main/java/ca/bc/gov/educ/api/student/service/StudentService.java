package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.repository.StudentMergeRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import ca.bc.gov.educ.api.student.repository.StudentTwinRepository;
import ca.bc.gov.educ.api.student.struct.StudentCreate;
import ca.bc.gov.educ.api.student.struct.StudentUpdate;
import ca.bc.gov.educ.api.student.util.TransformUtil;
import lombok.AccessLevel;
import lombok.Getter;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * StudentService
 *
 * @author John Cox
 */

@Service
public class StudentService {
  private static final String STUDENT_ID_ATTRIBUTE = "studentID";

  @Getter(AccessLevel.PRIVATE)
  private final StudentRepository repository;

  @Getter(AccessLevel.PRIVATE)
  private final StudentHistoryRepository studentHistoryRepository;

  @Getter(AccessLevel.PRIVATE)
  private final StudentMergeRepository studentMergeRepo;

  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinRepository studentTwinRepo;

  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  @Autowired
  public StudentService(final StudentRepository repository, StudentMergeRepository studentMergeRepo, StudentTwinRepository studentTwinRepo,
                        CodeTableService codeTableService, StudentHistoryRepository studentHistoryRepository) {
    this.repository = repository;
    this.studentMergeRepo = studentMergeRepo;
    this.studentTwinRepo = studentTwinRepo;
    this.codeTableService = codeTableService;
    this.studentHistoryRepository = studentHistoryRepository;
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
   * @param student the payload which will create the student record.
   * @return the saved instance.
   * @throws InvalidParameterException if Student GUID is passed in the payload for the post operation it will throw this exception.
   */
  public StudentEntity createStudent(StudentEntity student) {
    TransformUtil.uppercaseFields(student);
    return repository.save(student);
  }

  /**
   * Updates a StudentEntity
   *
   * @param studentUpdate the payload which will update the DB record for the given student.
   * @return the updated entity.
   * @throws EntityNotFoundException if the entity does not exist in the DB.
   */
  @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = {EntityNotFoundException.class})
  public StudentEntity updateStudent(StudentUpdate studentUpdate) {
    var student = StudentMapper.mapper.toModel(studentUpdate);
    Optional<StudentEntity> curStudentEntity = repository.findById(student.getStudentID());

    if (curStudentEntity.isPresent()) {
      createStudentHistory(studentUpdate, curStudentEntity.get());

      final StudentEntity newStudentEntity = curStudentEntity.get();
      val createUser = newStudentEntity.getCreateUser();
      val createDate = newStudentEntity.getCreateDate();
      BeanUtils.copyProperties(student, newStudentEntity);
      newStudentEntity.setCreateUser(createUser);
      newStudentEntity.setCreateDate(createDate);
      TransformUtil.uppercaseFields(newStudentEntity);
      return repository.save(newStudentEntity);
    } else {
      throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, student.getStudentID().toString());
    }
  }

  private void createStudentHistory(StudentUpdate studentUpdate, StudentEntity curStudentEntity) {
    final StudentHistoryEntity studentHistoryEntity = new StudentHistoryEntity();
    BeanUtils.copyProperties(curStudentEntity, studentHistoryEntity);
    studentHistoryEntity.setStudentID(curStudentEntity.getStudentID());
    studentHistoryEntity.setHistoryActivityCode(studentUpdate.getHistoryActivityCode());
    studentHistoryEntity.setCreateUser(studentUpdate.getCreateUser());
    studentHistoryEntity.setCreateDate(LocalDateTime.now());
    studentHistoryEntity.setUpdateUser(studentUpdate.getCreateUser());
    studentHistoryEntity.setUpdateDate(LocalDateTime.now());
    studentHistoryRepository.save(studentHistoryEntity);
  }


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
    getRepository().delete(entity);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<StudentEntity>> findAll(Specification<StudentEntity> studentSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
    try {
      val result = getRepository().findAll(studentSpecs, paging);
      return CompletableFuture.completedFuture(result);
    } catch (final Exception ex) {
      throw new CompletionException(ex);
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public StudentEntity createStudentWithAssociations(StudentCreate student) {
    StudentEntity studentEntity = StudentMapper.mapper.toModel(student);
    TransformUtil.uppercaseFields(studentEntity);
    getRepository().save(studentEntity);
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
        studentMergeEntity.setMergeStudent(getRepository().findById(UUID.fromString(mergeStudent.getMergeStudentID())).get());
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

  public List<GenderCodeEntity> getGenderCodesList() {
    return getCodeTableService().getGenderCodesList();
  }

  public List<SexCodeEntity> getSexCodesList() {
    return getCodeTableService().getSexCodesList();
  }

  public List<DemogCodeEntity> getDemogCodesList() {
    return getCodeTableService().getDemogCodesList();
  }

  public List<GradeCodeEntity> getGradeCodesList() {
    return getCodeTableService().getGradeCodesList();
  }

  public List<StatusCodeEntity> getStatusCodesList() {
    return getCodeTableService().getStatusCodesList();
  }

  public List<StudentHistoryActivityCodeEntity> getStudentHistoryActivityCodesList() {
    return getCodeTableService().getStudentHistoryActivityCodesList();
  }

  public Optional<GenderCodeEntity> findGenderCode(String genderCode) {
    return getCodeTableService().findGenderCode(genderCode);
  }

  public Optional<SexCodeEntity> findSexCode(String sexCode) {
    return getCodeTableService().findSexCode(sexCode);
  }

  public Optional<StudentHistoryActivityCodeEntity> findStudentHistoryActivityCode(String historyActivityCode) {
    return getCodeTableService().findStudentHistoryActivityCode(historyActivityCode);
  }
}
