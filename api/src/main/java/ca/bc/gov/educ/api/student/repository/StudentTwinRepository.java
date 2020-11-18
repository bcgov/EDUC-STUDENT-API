package ca.bc.gov.educ.api.student.repository;

import ca.bc.gov.educ.api.student.model.StudentTwinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface StudentTwinRepository extends JpaRepository<StudentTwinEntity, UUID>, JpaSpecificationExecutor<StudentTwinEntity> {

  List<StudentTwinEntity> findByStudentIDOrTwinStudentID(UUID studentID, UUID twinStudentID);

  List<StudentTwinEntity> findByTwinStudentID(UUID twinStudentID);

  List<StudentTwinEntity> findByStudentTwinIDIn(List<UUID> studentTwinIDs);
}
