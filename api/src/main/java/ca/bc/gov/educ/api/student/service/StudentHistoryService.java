package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.struct.NameVariant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Student History Service
 *
 */

@Service
public class StudentHistoryService {
  @Getter(AccessLevel.PRIVATE)
  private final StudentHistoryRepository studentHistoryRepository;

  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  @Autowired
  public StudentHistoryService(StudentHistoryRepository studentHistoryRepository, CodeTableService codeTableService) {
    this.studentHistoryRepository = studentHistoryRepository;
    this.codeTableService = codeTableService;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<StudentHistoryEntity>> findStudentHistoryByStudentID(final Integer pageNumber, final Integer pageSize,
                                                                                     final List<Sort.Order> sorts, final String studentID) {
    Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
    try {
      val result = getStudentHistoryRepository().findByStudentID(UUID.fromString(studentID), paging);
      return CompletableFuture.completedFuture(result);
    } catch (final Exception ex) {
      throw new CompletionException(ex);
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void createStudentHistory(StudentEntity curStudentEntity, String historyActivityCode, String manipulateUser) {
    final StudentHistoryEntity studentHistoryEntity = new StudentHistoryEntity();
    BeanUtils.copyProperties(curStudentEntity, studentHistoryEntity);
    studentHistoryEntity.setHistoryActivityCode(historyActivityCode);
    studentHistoryEntity.setCreateUser(manipulateUser);
    studentHistoryEntity.setCreateDate(LocalDateTime.now());
    studentHistoryEntity.setUpdateUser(manipulateUser);
    studentHistoryEntity.setUpdateDate(LocalDateTime.now());
    studentHistoryRepository.save(studentHistoryEntity);
  }

  public List<StudentHistoryActivityCodeEntity> getStudentHistoryActivityCodesList() {
    return getCodeTableService().getStudentHistoryActivityCodesList();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public Long deleteByStudentID(final UUID studentID) {
    return studentHistoryRepository.deleteByStudentID(studentID);
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public NameVariant findNameVariantByAuditHistory(
          final String legalFirstName, final String legalLastName, final String legalMiddleNames,
          final String usualFirstName, final String usualLastName, final String usualMiddleNames) {
    List<Map<String, Object>> studentNameList = getStudentHistoryRepository().findStudentNameByAuditHistory(
            StringUtils.isNotBlank(legalFirstName)? legalFirstName : null,
            StringUtils.isNotBlank(legalLastName)? legalLastName : null,
            StringUtils.isNotBlank(legalMiddleNames)? legalMiddleNames : null,
            StringUtils.isNotBlank(usualFirstName)? usualFirstName : null,
            StringUtils.isNotBlank(usualLastName)? usualLastName : null,
            StringUtils.isNotBlank(usualMiddleNames)? usualMiddleNames : null);
    return populateNameVariant(studentNameList);
  }

  private NameVariant populateNameVariant(List<Map<String, Object>> studentNameList) {
    NameVariant nameVariant = new NameVariant();

    List<String> legalFirstNames = new ArrayList<>();
    List<String> legalLastNames = new ArrayList<>();
    List<String> legalMiddleNames = new ArrayList<>();
    List<String> usualFirstNames = new ArrayList<>();
    List<String> usualLastNames = new ArrayList<>();
    List<String> usualMiddleNames = new ArrayList<>();

    for (Map<String, Object> row : studentNameList) {
      String legalFirstName = (String)row.get("LEGAL_FIRST_NAME");
      if (StringUtils.isNotBlank(legalFirstName)
        && !legalFirstNames.contains(legalFirstName)) {
        legalFirstNames.add(legalFirstName);
      }
      // Collect the Legal Last Name
      String legalLastName = (String)row.get("LEGAL_LAST_NAME");
      if (StringUtils.isNotBlank(legalLastName)
        && !legalLastNames.contains(legalLastName)) {
        legalLastNames.add(legalLastName);
      }
      // Collect the Legal Middle Names
      String legalMiddleName = (String)row.get("LEGAL_MIDDLE_NAMES");
      if (StringUtils.isNotBlank(legalMiddleName)
        && !legalMiddleNames.contains(legalMiddleName)) {
          legalMiddleNames.add(legalMiddleName);
      }
      // Collect the Usual First Name
      String usualFirstName = (String)row.get("USUAL_FIRST_NAME");
      if (StringUtils.isNotBlank(usualFirstName)
        && !usualFirstNames.contains(usualFirstName)) {
        usualFirstNames.add(usualFirstName);
      }
      // Collect the Usual Last Name
      String usualLastName = (String)row.get("USUAL_LAST_NAME");
      if (StringUtils.isNotBlank(usualLastName)
        && !usualLastNames.contains(usualLastName)) {
        usualLastNames.add(usualLastName);
      }
      // Collect the Usual Middle Names
      String usualMiddleName = (String)row.get("USUAL_MIDDLE_NAMES");
      if (StringUtils.isNotBlank(usualMiddleName)
        && !usualMiddleNames.contains(usualMiddleName)) {
        usualMiddleNames.add(usualMiddleName);
      }
    }

    nameVariant.setLegalFirstNames(legalFirstNames);
    nameVariant.setLegalLastNames(legalLastNames);
    nameVariant.setLegalMiddleNames(legalMiddleNames);
    nameVariant.setUsualFirstNames(usualFirstNames);
    nameVariant.setUsualLastNames(usualLastNames);
    nameVariant.setUsualMiddleNames(usualMiddleNames);

    return nameVariant;
  }

}
