package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/**
 * The interface Student history repository.
 */
public interface StudentHistoryRepository extends JpaRepository<StudentHistoryEntity, UUID>, JpaSpecificationExecutor<StudentHistoryEntity>, StudentHistoryRepositoryCustom {
  /**
   * Find by student id page.
   *
   * @param studentID the student id
   * @param pageable  the pageable
   * @return the page
   */
  Page<StudentHistoryEntity> findByStudentID(UUID studentID, Pageable pageable);

  /**
   * Find by student id.
   *
   * @param studentID the student id
   * @return list
   */
  List<StudentHistoryEntity> findByStudentID(UUID studentID);

  /**
   * Delete by student id long.
   *
   * @param studentID the student id
   * @return the long
   */
  Long deleteByStudentID(UUID studentID);
}
