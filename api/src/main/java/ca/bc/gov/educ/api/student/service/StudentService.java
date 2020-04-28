package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.model.GenderCodeEntity;
import ca.bc.gov.educ.api.student.model.SexCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.repository.GenderCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.SexCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
  private final GenderCodeTableRepository genderCodeTableRepo;

  @Getter(AccessLevel.PRIVATE)
  private final SexCodeTableRepository sexCodeTableRepo;

  public StudentService(@Autowired final StudentRepository repository, @Autowired final GenderCodeTableRepository genderCodeTableRepo, @Autowired final SexCodeTableRepository sexCodeTableRepo) {
    this.repository = repository;
    this.sexCodeTableRepo = sexCodeTableRepo;
    this.genderCodeTableRepo = genderCodeTableRepo;
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
   * Returns the full list of access channel codes
   *
   * @return {@link List<SexCodeEntity>}
   */
  @Cacheable("sexCodes")
  public List<SexCodeEntity> getSexCodesList() {
    return sexCodeTableRepo.findAll();
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
  public void deleteAll() {
    getRepository().deleteAll();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteById(UUID id) {
    val entityOptional = getRepository().findById(id);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(StudentEntity.class, STUDENT_ID_ATTRIBUTE, id.toString()));
    getRepository().delete(entity);
  }
}
