package ca.bc.gov.educ.api.student.repository;

import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentHistoryRepository extends JpaRepository<StudentHistoryEntity, UUID> {
  Page<StudentHistoryEntity> findByStudentID(UUID studentID, Pageable pageable);
  Long deleteByStudentID(UUID studentID);
}
