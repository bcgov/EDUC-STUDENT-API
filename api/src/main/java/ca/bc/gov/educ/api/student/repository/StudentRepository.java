package ca.bc.gov.educ.api.student.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import ca.bc.gov.educ.api.student.model.StudentEntity;

public interface StudentRepository extends CrudRepository<StudentEntity, UUID> {
  Optional<StudentEntity> findStudentEntityByPen(String pen);

  Optional<StudentEntity> findStudentEntityByEmail(String email);

}
