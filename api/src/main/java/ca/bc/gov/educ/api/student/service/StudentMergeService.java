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
public class StudentMergeService {

  private final StudentMergeRepository studentMergeRepo;

  private final StudentMergeDirectionCodeTableRepository studentMergeDirectionCodeTableRepo;

  private final StudentMergeSourceCodeTableRepository studentMergeSourceCodeTableRepo;

  @Autowired
  public StudentMergeService(final StudentMergeRepository studentMergeRepo, final StudentMergeDirectionCodeTableRepository studentMergeDirectionCodeTableRepo,
                             final StudentMergeSourceCodeTableRepository studentMergeSourceCodeTableRepo) {
    this.studentMergeRepo = studentMergeRepo;
    this.studentMergeDirectionCodeTableRepo = studentMergeDirectionCodeTableRepo;
    this.studentMergeSourceCodeTableRepo = studentMergeSourceCodeTableRepo;
  }

  /**
   * Returns the list of student merge record
   *
   * @return {@link List<StudentMergeEntity>}
   */
  public List<StudentMergeEntity> findStudentMerges(UUID studentID) {
    return studentMergeRepo.findStudentMergeEntityByStudentID(studentID);
  }

  /**
   * Creates a StudentMergeEntity
   *
   * @param studentMerge the payload which will create the student merge record.
   * @return the saved instance.
   * @throws InvalidParameterException if Student Merge GUID is passed in the payload for the post operation it will throw this exception.
   */
  public StudentMergeEntity createStudentMerge(StudentMergeEntity studentMerge) {
    return studentMergeRepo.save(studentMerge);
  }

  /**
  * Returns the full list of student merge direction codes
  *
  * @return {@link List<StudentMergeDirectionCodeEntity>}
  */
  @Cacheable("mergeDirectionCodes")
  public List<StudentMergeDirectionCodeEntity> getStudentMergeDirectionCodesList() {
    return studentMergeDirectionCodeTableRepo.findAll();
  }

  public Optional<StudentMergeDirectionCodeEntity> findStudentMergeDirectionCode(String mergeDirectionCode) {
    return Optional.ofNullable(loadStudentMergeDirectionCodes().get(mergeDirectionCode));
  }

  private Map<String, StudentMergeDirectionCodeEntity> loadStudentMergeDirectionCodes() {
    return getStudentMergeDirectionCodesList().stream().collect(Collectors.toMap(StudentMergeDirectionCodeEntity::getMergeDirectionCode, mergeDirectionCodeEntity -> mergeDirectionCodeEntity));
  }

  /**
  * Returns the full list of student merge source codes
  *
  * @return {@link List<StudentMergeSourceCodeEntity>}
  */
  @Cacheable("mergeSourceCodes")
  public List<StudentMergeSourceCodeEntity> getStudentMergeSourceCodesList() {
    return studentMergeSourceCodeTableRepo.findAll();
  }

  public Optional<StudentMergeSourceCodeEntity> findStudentMergeSourceCode(String mergeSourceCode) {
    return Optional.ofNullable(loadStudentMergeSourceCodes().get(mergeSourceCode));
  }

  private Map<String, StudentMergeSourceCodeEntity> loadStudentMergeSourceCodes() {
    return getStudentMergeSourceCodesList().stream().collect(Collectors.toMap(StudentMergeSourceCodeEntity::getMergeSourceCode, mergeSourceCodeEntity -> mergeSourceCodeEntity));
  }

}
