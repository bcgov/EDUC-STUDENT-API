package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.InvalidParameterException;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;

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

  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinRepository studentTwinRepo;

  @Getter(AccessLevel.PRIVATE)
  private final StudentTwinReasonCodeTableRepository studentTwinReasonCodeTableRepo;

  public StudentTwinService(@Autowired final StudentTwinRepository studentTwinRepo, @Autowired final StudentTwinReasonCodeTableRepository studentTwinReasonCodeTableRepo) {
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
