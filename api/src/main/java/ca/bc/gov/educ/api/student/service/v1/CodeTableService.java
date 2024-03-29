package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.model.v1.*;
import ca.bc.gov.educ.api.student.repository.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Code table service.
 */
@Service
public class CodeTableService {

  private final GenderCodeTableRepository genderCodeTableRepo;

  private final SexCodeTableRepository sexCodeTableRepo;

  private final DemogCodeTableRepository demogCodeTableRepo;

  private final StatusCodeTableRepository statusCodeTableRepo;

  private final GradeCodeTableRepository gradeCodeTableRepo;

  private final StudentHistoryActivityCodeTableRepository historyActivityCodeTableRepo;

  private final DocumentTypeCodeRepository documentTypeCodeRepository;

  /**
   * Instantiates a new Code table service.
   *
   * @param genderCodeTableRepo          the gender code table repo
   * @param sexCodeTableRepo             the sex code table repo
   * @param demogCodeTableRepo           the demog code table repo
   * @param statusCodeTableRepo          the status code table repo
   * @param gradeCodeTableRepo           the grade code table repo
   * @param historyActivityCodeTableRepo the history activity code table repo
   * @param documentTypeCodeRepository   the document type code repository
   */
  @Autowired
  public CodeTableService(GenderCodeTableRepository genderCodeTableRepo, SexCodeTableRepository sexCodeTableRepo, DemogCodeTableRepository demogCodeTableRepo,
                          StatusCodeTableRepository statusCodeTableRepo, GradeCodeTableRepository gradeCodeTableRepo, StudentHistoryActivityCodeTableRepository historyActivityCodeTableRepo, DocumentTypeCodeRepository documentTypeCodeRepository) {
    this.genderCodeTableRepo = genderCodeTableRepo;
    this.sexCodeTableRepo = sexCodeTableRepo;
    this.demogCodeTableRepo = demogCodeTableRepo;
    this.statusCodeTableRepo = statusCodeTableRepo;
    this.gradeCodeTableRepo = gradeCodeTableRepo;
    this.historyActivityCodeTableRepo = historyActivityCodeTableRepo;
    this.documentTypeCodeRepository = documentTypeCodeRepository;
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

  /**
   * Returns the full list of document Type Codes
   *
   * @return {@link List<GenderCodeEntity>}
   */
  @Cacheable("documentTypeCodes")
  public List<DocumentTypeCodeEntity> getDocumentTypeCodes() {
    return documentTypeCodeRepository.findAll();
  }


  /**
   * Find Document Type code optional.
   *
   * @param documentTypeCode the Document Type Code
   * @return the optional
   */
  public Optional<DocumentTypeCodeEntity> findDocumentTypeCode(String documentTypeCode) {
    return Optional.ofNullable(loadAllDocumentTypeCodes().get(documentTypeCode));
  }

  /**
   * Find sex code optional.
   *
   * @param sexCode the sex code
   * @return the optional
   */
  public Optional<SexCodeEntity> findSexCode(String sexCode) {
    return Optional.ofNullable(loadAllSexCodes().get(sexCode));
  }

  /**
   * Find gender code optional.
   *
   * @param genderCode the gender code
   * @return the optional
   */
  public Optional<GenderCodeEntity> findGenderCode(String genderCode) {
    return Optional.ofNullable(loadGenderCodes().get(genderCode));
  }

  /**
   * Find student history activity code optional.
   *
   * @param historyActivityCode the history activity code
   * @return the optional
   */
  public Optional<StudentHistoryActivityCodeEntity> findStudentHistoryActivityCode(String historyActivityCode) {
    return Optional.ofNullable(loadStudentHistoryActivityCodes().get(historyActivityCode));
  }

  /**
   * Returns the full list of student history activity codes
   *
   * @return {@link List<StudentHistoryActivityCodeEntity>}
   */
  @Cacheable("studentHistoryActivityCodes")
  public List<StudentHistoryActivityCodeEntity> getStudentHistoryActivityCodesList() {
    return historyActivityCodeTableRepo.findAll();
  }

  private Map<String, SexCodeEntity> loadAllSexCodes() {
    return getSexCodesList().stream().collect(Collectors.toMap(SexCodeEntity::getSexCode, Function.identity()));
  }

  private Map<String, DocumentTypeCodeEntity> loadAllDocumentTypeCodes() {
    return getDocumentTypeCodes().stream().collect(Collectors.toMap(DocumentTypeCodeEntity::getDocumentTypeCode, Function.identity()));
  }

  private Map<String, GenderCodeEntity> loadGenderCodes() {
    return getGenderCodesList().stream().collect(Collectors.toMap(GenderCodeEntity::getGenderCode, Function.identity()));
  }

  private Map<String, StudentHistoryActivityCodeEntity> loadStudentHistoryActivityCodes() {
    return getStudentHistoryActivityCodesList().stream().collect(Collectors.toMap(StudentHistoryActivityCodeEntity::getHistoryActivityCode, Function.identity()));
  }
}
