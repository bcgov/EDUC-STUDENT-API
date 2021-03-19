package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Student repository.
 */
public interface StudentRepository extends CrudRepository<StudentEntity, UUID>, JpaSpecificationExecutor<StudentEntity> {
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

  /**
   * Find student entity by email optional.
   *
   * @param email the email
   * @return the optional
   */
  Optional<StudentEntity> findStudentEntityByEmail(String email);

  List<StudentEntity> findAll();

}
