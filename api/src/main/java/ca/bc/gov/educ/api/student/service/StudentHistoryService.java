package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.repository.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.repository.StudentRepository;
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
import java.util.Optional;
import java.util.UUID;
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
  private final StudentRepository studentRepository;

  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  @Autowired
  public StudentHistoryService(StudentHistoryRepository studentHistoryRepository, StudentRepository studentRepository, CodeTableService codeTableService) {
    this.studentHistoryRepository = studentHistoryRepository;
    this.studentRepository = studentRepository;
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
    List<StudentHistoryEntity> studentHistoryList = getStudentHistoryRepository().findByNames(
            StringUtils.isNotBlank(legalFirstName)? legalFirstName : null,
            StringUtils.isNotBlank(legalLastName)? legalLastName : null,
            StringUtils.isNotBlank(legalMiddleNames)? legalMiddleNames : null,
            StringUtils.isNotBlank(usualFirstName)? usualFirstName : null,
            StringUtils.isNotBlank(usualLastName)? usualLastName : null,
            StringUtils.isNotBlank(usualMiddleNames)? usualMiddleNames : null);
    return populateNameVariant(studentHistoryList);
  }

  private NameVariant populateNameVariant(List<StudentHistoryEntity> studentHistoryList) {
    NameVariant nameVariant = new NameVariant();

    List<String> legalFirstNames = new ArrayList<>();
    List<String> legalLastNames = new ArrayList<>();
    List<String> legalMiddleNames = new ArrayList<>();
    List<String> usualFirstNames = new ArrayList<>();
    List<String> usualLastNames = new ArrayList<>();
    List<String> usualMiddleNames = new ArrayList<>();

    // Find the current student names
    for (StudentHistoryEntity hist : studentHistoryList) {
      Optional<StudentEntity> student = getStudentRepository().findById(hist.getStudentID());
      StudentEntity studentEntity;
      if (student.isPresent()) {
        studentEntity = student.get();
      } else {
        throw new EntityNotFoundException(StudentEntity.class, "studentID", hist.getStudentID().toString());
      }
      // Collect the Legal First Name
      if (StringUtils.isNotBlank(studentEntity.getLegalFirstName())
        && !legalFirstNames.contains(studentEntity.getLegalFirstName())) {
        legalFirstNames.add(studentEntity.getLegalFirstName());
      }
      // Collect the Legal Last Name
      if (StringUtils.isNotBlank(studentEntity.getLegalLastName())
        && !legalLastNames.contains(studentEntity.getLegalLastName())) {
        legalLastNames.add(studentEntity.getLegalLastName());
      }
      // Collect the Legal Middle Names
      if (StringUtils.isNotBlank(studentEntity.getLegalMiddleNames())
        && !legalMiddleNames.contains(studentEntity.getLegalMiddleNames())) {
          legalMiddleNames.add(studentEntity.getLegalMiddleNames());
      }
      // Collect the Usual First Name
      if (StringUtils.isNotBlank(studentEntity.getUsualFirstName())
        && !usualFirstNames.contains(studentEntity.getUsualFirstName())) {
        usualFirstNames.add(studentEntity.getUsualFirstName());
      }
      // Collect the Usual Last Name
      if (StringUtils.isNotBlank(studentEntity.getUsualLastName())
        && !usualLastNames.contains(studentEntity.getUsualLastName())) {
        usualLastNames.add(studentEntity.getUsualLastName());
      }
      // Collect the Usual Middle Names
      if (StringUtils.isNotBlank(studentEntity.getUsualMiddleNames())
        && !usualMiddleNames.contains(studentEntity.getUsualMiddleNames())) {
        usualMiddleNames.add(studentEntity.getUsualMiddleNames());
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
