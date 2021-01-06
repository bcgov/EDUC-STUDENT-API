package ca.bc.gov.educ.api.student.repository.v1;

import java.util.List;
import java.util.UUID;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import ca.bc.gov.educ.api.student.model.v1.StudentMergeEntity;

public interface StudentMergeRepository extends CrudRepository<StudentMergeEntity, UUID>, JpaSpecificationExecutor<StudentMergeEntity> {
  List<StudentMergeEntity> findStudentMergeEntityByStudentID(UUID studentID);

  List<StudentMergeEntity> findStudentMergeEntityByMergeStudent(StudentEntity studentEntity);

  List<StudentMergeEntity> findStudentMergeEntityByStudentIDAndStudentMergeDirectionCode(UUID studentID, String studentMergeDirectionCode);
}
