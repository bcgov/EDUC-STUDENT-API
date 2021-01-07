package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StudentHistoryRepository extends JpaRepository<StudentHistoryEntity, UUID>, JpaSpecificationExecutor<StudentHistoryEntity> {
  Page<StudentHistoryEntity> findByStudentID(UUID studentID, Pageable pageable);
  Long deleteByStudentID(UUID studentID);
}
