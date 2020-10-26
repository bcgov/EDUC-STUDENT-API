package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CodeTableService {

  private final GenderCodeTableRepository genderCodeTableRepo;

  private final SexCodeTableRepository sexCodeTableRepo;

  private final DemogCodeTableRepository demogCodeTableRepo;

  private final StatusCodeTableRepository statusCodeTableRepo;

  private final GradeCodeTableRepository gradeCodeTableRepo;

  @Autowired
  public CodeTableService(GenderCodeTableRepository genderCodeTableRepo, SexCodeTableRepository sexCodeTableRepo, DemogCodeTableRepository demogCodeTableRepo, StatusCodeTableRepository statusCodeTableRepo, GradeCodeTableRepository gradeCodeTableRepo) {
    this.genderCodeTableRepo = genderCodeTableRepo;
    this.sexCodeTableRepo = sexCodeTableRepo;
    this.demogCodeTableRepo = demogCodeTableRepo;
    this.statusCodeTableRepo = statusCodeTableRepo;
    this.gradeCodeTableRepo = gradeCodeTableRepo;
  }

  /**
   * Returns the full list of sex codes
   *
   * @return {@link List <SexCodeEntity>}
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

}
