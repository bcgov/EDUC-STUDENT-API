package ca.bc.gov.educ.api.student.service.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryActivityCodeEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.repository.v1.StudentHistoryRepository;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import org.jboss.threads.EnhancedQueueExecutor;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Student History Service
 */
@Service
public class StudentHistoryService {
  private final Executor paginatedQueryExecutor = new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async-history-pagination-query-executor-%d").build())
      .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
  @Getter(AccessLevel.PRIVATE)
  private final StudentHistoryRepository studentHistoryRepository;

  @Getter(AccessLevel.PRIVATE)
  private final CodeTableService codeTableService;

  /**
   * Instantiates a new Student history service.
   *
   * @param studentHistoryRepository the student history repository
   * @param codeTableService         the code table service
   */
  @Autowired
  public StudentHistoryService(StudentHistoryRepository studentHistoryRepository, CodeTableService codeTableService) {
    this.studentHistoryRepository = studentHistoryRepository;
    this.codeTableService = codeTableService;
  }

  /**
   * Find student history by student id completable future.
   *
   * @param pageNumber the page number
   * @param pageSize   the page size
   * @param sorts      the sorts
   * @param studentID  the student id
   * @return the completable future
   */
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
    }, paginatedQueryExecutor);
  }

  /**
   * Find all completable future.
   *
   * @param studentHistorySpecs the student history specs
   * @param pageNumber          the page number
   * @param pageSize            the page size
   * @param sorts               the sorts
   * @return the completable future
   */
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
    }, paginatedQueryExecutor);
  }

  /**
   * Create student history.
   *
   * @param curStudentEntity    the cur student entity
   * @param historyActivityCode the history activity code
   * @param updateUser      the manipulate user
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public StudentHistoryEntity createStudentHistory(StudentEntity curStudentEntity, String historyActivityCode, String updateUser) {
    final StudentHistoryEntity studentHistoryEntity = new StudentHistoryEntity();
    BeanUtils.copyProperties(curStudentEntity, studentHistoryEntity);
    studentHistoryEntity.setHistoryActivityCode(historyActivityCode);
    studentHistoryEntity.setCreateUser(updateUser);
    studentHistoryEntity.setCreateDate(LocalDateTime.now());
    studentHistoryEntity.setUpdateUser(updateUser);
    studentHistoryEntity.setUpdateDate(LocalDateTime.now());
    return studentHistoryRepository.save(studentHistoryEntity);
  }

  /**
   * Create student history.
   *
   * @param studentHistory    the cur student history entity
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public StudentHistoryEntity createStudentHistory(StudentHistory studentHistory) {
    StudentEntity studentEntity = new StudentEntity();
    BeanUtils.copyProperties(studentHistory, studentEntity);
    return createStudentHistory(studentEntity, studentHistory.getHistoryActivityCode(), studentHistory.getUpdateUser());
  }

  /**
   * Gets student history activity codes list.
   *
   * @return the student history activity codes list
   */
  public List<StudentHistoryActivityCodeEntity> getStudentHistoryActivityCodesList() {
    return getCodeTableService().getStudentHistoryActivityCodesList();
  }

  /**
   * Delete by student id long.
   *
   * @param studentID the student id
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteByStudentID(final UUID studentID) {
    studentHistoryRepository.deleteByStudentID(studentID);
  }
}
