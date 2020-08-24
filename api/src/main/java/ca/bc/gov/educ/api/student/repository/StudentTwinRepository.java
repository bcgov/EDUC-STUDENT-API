package ca.bc.gov.educ.api.student.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import ca.bc.gov.educ.api.student.model.StudentTwinEntity;

public interface StudentTwinRepository extends CrudRepository<StudentTwinEntity, UUID>, JpaSpecificationExecutor<StudentTwinEntity> {
  List<StudentTwinEntity> findStudentTwinEntityByStudentID(UUID studentID);
}
