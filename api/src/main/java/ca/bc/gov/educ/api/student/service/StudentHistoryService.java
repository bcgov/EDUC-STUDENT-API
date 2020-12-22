package ca.bc.gov.educ.api.student.service;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentHistoryActivityCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.repository.StudentHistoryRepository;
import lombok.AccessLevel;
import lombok.Getter;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Student History Service
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
    return CompletableFuture.supplyAsync(() -> {
      Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
      try {
        return getStudentHistoryRepository().findByStudentID(UUID.fromString(studentID), paging);
      } catch (final Exception ex) {
        throw new CompletionException(ex);
      }
    });
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<StudentHistoryEntity>> findAll(Specification<StudentHistoryEntity> studentHistorySpecs, final Integer pageNumber,
                                                               final Integer pageSize, final List<Sort.Order> sorts) {
    return CompletableFuture.supplyAsync(() -> {
      Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
      try {
        return getStudentHistoryRepository().findAll(studentHistorySpecs, paging);
      } catch (final Exception ex) {
        throw new CompletionException(ex);
      }
    });
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
}
