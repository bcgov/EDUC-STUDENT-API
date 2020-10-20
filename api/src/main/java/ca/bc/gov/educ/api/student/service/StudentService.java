package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.mappers.StudentMapper;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
import ca.bc.gov.educ.api.student.struct.Student;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

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
  private final StudentMergeRepository studentMergeRepo;

  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinRepository studentTwinRepo;

  private final GenderCodeTableRepository genderCodeTableRepo;

  private final SexCodeTableRepository sexCodeTableRepo;

  private final DemogCodeTableRepository demogCodeTableRepo;

  private final StatusCodeTableRepository statusCodeTableRepo;

  private final GradeCodeTableRepository gradeCodeTableRepo;

  @Autowired
  public StudentService(final StudentRepository repository, StudentMergeRepository studentMergeRepo, StudentTwinRepository studentTwinRepo, final GenderCodeTableRepository genderCodeTableRepo, final SexCodeTableRepository sexCodeTableRepo,
                        final StatusCodeTableRepository statusCodeTableRepo, final DemogCodeTableRepository demogCodeTableRepo, final GradeCodeTableRepository gradeCodeTableRepo) {
    this.repository = repository;
    this.studentMergeRepo = studentMergeRepo;
    this.studentTwinRepo = studentTwinRepo;
    this.sexCodeTableRepo = sexCodeTableRepo;
    this.genderCodeTableRepo = genderCodeTableRepo;
    this.statusCodeTableRepo = statusCodeTableRepo;
    this.demogCodeTableRepo = demogCodeTableRepo;
    this.gradeCodeTableRepo = gradeCodeTableRepo;
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
    return repository.save(student);
  }

  /**
   * Updates a StudentEntity
   *
   * @param student the payload which will update the DB record for the given student.
   * @return the updated entity.
   * @throws EntityNotFoundException if the entity does not exist in the DB.
   */
  public StudentEntity updateStudent(StudentEntity student) {

    Optional<StudentEntity> curStudentEntity = repository.findById(student.getStudentID());

    if (curStudentEntity.isPresent()) {
      final StudentEntity newStudentEntity = curStudentEntity.get();
      val createUser = newStudentEntity.getCreateUser();
      val createDate = newStudentEntity.getCreateDate();
      BeanUtils.copyProperties(student, newStudentEntity);
      newStudentEntity.setCreateUser(createUser);
      newStudentEntity.setCreateDate(createDate);
      return repository.save(newStudentEntity);
    } else {
      throw new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, student.getStudentID().toString());
    }
  }


  /**
   * Returns the full list of sex codes
   *
   * @return {@link List<SexCodeEntity>}
   */
  @Cacheable("sexCodes")
  public List<SexCodeEntity> getSexCodesList() {
    return sexCodeTableRepo.findAll();
  }

  /**
   * Returns the full list of demog codes
   *
   * @return {@link List<DemogCodeEntity>}
   */
  @Cacheable("demogCodes")
  public List<DemogCodeEntity> getDemogCodesList() {
    return demogCodeTableRepo.findAll();
  }

  /**
   * Returns the full list of demog codes
   *
   * @return {@link List<DemogCodeEntity>}
   */
  @Cacheable("gradeCodes")
  public List<GradeCodeEntity> getGradeCodesList() {
    return gradeCodeTableRepo.findAll();
  }

  /**
   * Returns the full list of status codes
   *
   * @return {@link List<StatusCodeEntity>}
   */
  @Cacheable("statusCodes")
  public List<StatusCodeEntity> getStatusCodesList() {
    return statusCodeTableRepo.findAll();
  }

  /**
   * Returns the full list of access channel codes
   *
   * @return {@link List<GenderCodeEntity>}
   */
  @Cacheable("genderCodes")
  public List<GenderCodeEntity> getGenderCodesList() {
    return genderCodeTableRepo.findAll();
  }

  public Optional<SexCodeEntity> findSexCode(String sexCode) {
    return Optional.ofNullable(loadAllSexCodes().get(sexCode));
  }

  public Optional<GenderCodeEntity> findGenderCode(String genderCode) {
    return Optional.ofNullable(loadGenderCodes().get(genderCode));
  }

  private Map<String, SexCodeEntity> loadAllSexCodes() {
    return getSexCodesList().stream().collect(Collectors.toMap(SexCodeEntity::getSexCode, sexCode -> sexCode));
  }


  private Map<String, GenderCodeEntity> loadGenderCodes() {
    return getGenderCodesList().stream().collect(Collectors.toMap(GenderCodeEntity::getGenderCode, genderCodeEntity -> genderCodeEntity));
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteById(UUID id) {
    val entityOptional = getRepository().findById(id);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, id.toString()));
    var twins = getStudentTwinRepo().findStudentTwinEntityByStudentIDOrTwinStudent_StudentID(entity.getStudentID(), entity.getStudentID());
    var twins2 = getStudentTwinRepo().findByTwinStudent(entity);
    if (!twins.isEmpty()) {
      getStudentTwinRepo().deleteAll(twins);
    }
    if (!twins2.isEmpty()) {
      getStudentTwinRepo().deleteAll(twins2);
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
  public StudentEntity createStudentWithAssociations(Student student) {
    StudentEntity studentEntity = StudentMapper.mapper.toModel(student);
    getRepository().save(studentEntity);
    if(!CollectionUtils.isEmpty(student.getStudentMergeAssociations())) {
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
    if(!CollectionUtils.isEmpty(student.getStudentTwinAssociations())) {
      List<StudentTwinEntity> studentTwinEntities = new ArrayList<>();
      for (var twinStudent : student.getStudentTwinAssociations()) {
        var studentTwinEntity = new StudentTwinEntity();
        studentTwinEntity.setCreateDate(studentEntity.getCreateDate());
        studentTwinEntity.setUpdateDate(studentEntity.getUpdateDate());
        studentTwinEntity.setCreateUser(studentEntity.getCreateUser());
        studentTwinEntity.setUpdateUser(studentEntity.getUpdateUser());
        studentTwinEntity.setStudentTwinReasonCode(twinStudent.getStudentTwinReasonCode());
        studentTwinEntity.setTwinStudent(getRepository().findById(UUID.fromString(twinStudent.getTwinStudentID())).get());
        studentTwinEntity.setStudentID(studentEntity.getStudentID());
        studentTwinEntities.add(studentTwinEntity);
      }
      getStudentTwinRepo().saveAll(studentTwinEntities);
    }

    return studentEntity;
  }
}
