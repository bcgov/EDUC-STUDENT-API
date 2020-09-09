package ca.bc.gov.educ.api.student.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import ca.bc.gov.educ.api.student.model.StudentMergeEntity;

public interface StudentMergeRepository extends CrudRepository<StudentMergeEntity, UUID>, JpaSpecificationExecutor<StudentMergeEntity> {
  List<StudentMergeEntity> findStudentMergeEntityByStudentID(UUID studentID);
  List<StudentMergeEntity> findStudentMergeEntityByStudentIDAndStudentMergeDirectionCode(UUID studentID, String studentMergeDirectionCode);
}
