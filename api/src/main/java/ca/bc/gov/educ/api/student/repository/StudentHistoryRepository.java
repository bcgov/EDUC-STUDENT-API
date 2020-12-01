package ca.bc.gov.educ.api.student.repository;

import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StudentHistoryRepository extends JpaRepository<StudentHistoryEntity, UUID> {
  Page<StudentHistoryEntity> findByStudentID(UUID studentID, Pageable pageable);
  Long deleteByStudentID(UUID studentID);

  @Query(value = "SELECT DISTINCT s.legal_first_name, s.legal_last_name, s.legal_middle_names, s.usual_first_name, s.usual_last_name, s.usual_middle_names"
          +" FROM student_history h LEFT JOIN student s ON h.student_id = s.student_id"
          +" WHERE (:legalFirstName is null or h.legal_first_name = :legalFirstName)"
          + " AND  (:legalLastName is null or h.legal_last_name = :legalLastName)"
          + " AND  (:legalMiddleNames is null or h.legal_middle_names = :legalMiddleNames)"
          + " AND  (:usualFirstName is null or h.usual_first_name = :usualFirstName)"
          + " AND  (:usualLastName is null or h.usual_last_name = :usualLastName)"
          + " AND  (:usualMiddleNames is null or h.usual_middle_names = :usualMiddleNames)"
        , nativeQuery = true)
  List<Map<String, Object>> findStudentNameByAuditHistory(
          @Param("legalFirstName") String legalFirstName,
          @Param("legalLastName") String legalLastName,
          @Param("legalMiddleNames") String legalMiddleNames,
          @Param("usualFirstName") String usualFirstName,
          @Param("usualLastName") String usualLastName,
          @Param("usualMiddleNames") String usualMiddleNames);
}
