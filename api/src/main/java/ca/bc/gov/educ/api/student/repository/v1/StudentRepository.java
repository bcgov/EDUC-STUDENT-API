package ca.bc.gov.educ.api.student.repository.v1;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;

public interface StudentRepository extends CrudRepository<StudentEntity, UUID>, JpaSpecificationExecutor<StudentEntity> {
  Optional<StudentEntity> findStudentEntityByPen(String pen);

  Optional<StudentEntity> findStudentEntityByEmail(String email);
  List<StudentEntity> findAll();

}