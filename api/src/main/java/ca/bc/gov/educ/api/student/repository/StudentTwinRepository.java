package ca.bc.gov.educ.api.student.repository;

import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface StudentTwinRepository extends CrudRepository<StudentTwinEntity, UUID>, JpaSpecificationExecutor<StudentTwinEntity> {
  List<StudentTwinEntity> findStudentTwinEntityByStudentIDOrTwinStudent_StudentID(UUID studentID, UUID twinStudentID);

  List<StudentTwinEntity> findByTwinStudent(StudentEntity studentEntity);
}
