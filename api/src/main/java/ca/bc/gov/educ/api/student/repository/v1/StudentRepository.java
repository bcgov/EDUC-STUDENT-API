package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Student repository.
 */
public interface StudentRepository extends JpaRepository<StudentEntity, UUID>, JpaSpecificationExecutor<StudentEntity> {
  /**
   * Find student entity by pen optional.
   *
   * @param pen the pen
   * @return the optional
   */
  Optional<StudentEntity> findStudentEntityByPen(String pen);

  /**
   * Find student entities by student ids.
   *
   * @param studentID the list of student ids
   * @return the list of student entities
   */
  List<StudentEntity> findStudentEntityByStudentIDIn(List<UUID> studentID);

}
