package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.model.StudentMergeDirectionCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import ca.bc.gov.educ.api.student.model.StudentTwinReasonCodeEntity;
import ca.bc.gov.educ.api.student.repository.StudentTwinReasonCodeTableRepository;
import ca.bc.gov.educ.api.student.repository.StudentTwinRepository;
import lombok.val;
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
 * StudentMergeService
 *
 * @author Mingwei
 */

@Service
public class StudentTwinService {
  private static final String STUDENT_TWIN_ID_ATTRIBUTE = "studentTwinID";

  private final StudentTwinRepository studentTwinRepo;

  private final StudentTwinReasonCodeTableRepository studentTwinReasonCodeTableRepo;

  @Autowired
  public StudentTwinService(final StudentTwinRepository studentTwinRepo, final StudentTwinReasonCodeTableRepository studentTwinReasonCodeTableRepo) {
    this.studentTwinRepo = studentTwinRepo;
    this.studentTwinReasonCodeTableRepo = studentTwinReasonCodeTableRepo;
  }

  /**
   * Returns the list of student twin record
   *
   * @return {@link List<StudentTwinEntity>}
   */
  public List<StudentTwinEntity> findStudentTwins(UUID studentID) {
    return studentTwinRepo.findStudentTwinEntityByStudentID(studentID);
  }

  /**
   * Creates a StudentTwinEntity
   *
   * @param studentTwin the payload which will create the student twin record.
   * @return the saved instance.
   * @throws InvalidParameterException if Student Twin GUID is passed in the payload for the post operation it will throw this exception.
   */
  public StudentTwinEntity createStudentTwin(StudentTwinEntity studentTwin) {
    return studentTwinRepo.save(studentTwin);
  }

  /**
   * Deletes a student twin by ID
   *
   * @param id Student Twin ID
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteById(UUID id) {
    val entityOptional = studentTwinRepo.findById(id);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(StudentTwinEntity.class, STUDENT_TWIN_ID_ATTRIBUTE, id.toString()));

    studentTwinRepo.delete(entity);
  }

  /**
   * Returns the full list of student twin reason codes
   *
   * @return {@link List<StudentMergeDirectionCodeEntity>}
   */
  @Cacheable("twinReasonCodes")
  public List<StudentTwinReasonCodeEntity> getStudentTwinReasonCodesList() {
    return studentTwinReasonCodeTableRepo.findAll();
  }

  public Optional<StudentTwinReasonCodeEntity> findStudentTwinReasonCode(String twinReasonCode) {
    return Optional.ofNullable(loadStudentTwinReasonCodes().get(twinReasonCode));
  }

  private Map<String, StudentTwinReasonCodeEntity> loadStudentTwinReasonCodes() {
    return getStudentTwinReasonCodesList().stream().collect(Collectors.toMap(StudentTwinReasonCodeEntity::getTwinReasonCode, twinReasonCodeEntity -> twinReasonCodeEntity));
  }

}
