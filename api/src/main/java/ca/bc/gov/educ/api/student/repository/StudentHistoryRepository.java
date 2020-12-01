package ca.bc.gov.educ.api.student.repository;

import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StudentHistoryRepository extends JpaRepository<StudentHistoryEntity, UUID> {
  Page<StudentHistoryEntity> findByStudentID(UUID studentID, Pageable pageable);
  Long deleteByStudentID(UUID studentID);

  @Query(value = "SELECT h FROM StudentHistoryEntity h "
    +" WHERE (:legalFirstName is null or h.legalFirstName = :legalFirstName)"
    + " AND  (:legalLastName is null or h.legalLastName = :legalLastName)"
    + " AND  (:legalMiddleNames is null or h.legalMiddleNames = :legalMiddleNames)"
    + " AND  (:usualFirstName is null or h.usualFirstName = :usualFirstName)"
    + " AND  (:usualLastName is null or h.usualLastName = :usualLastName)"
    + " AND  (:usualMiddleNames is null or h.usualMiddleNames = :usualMiddleNames)")
  List<StudentHistoryEntity> findByNames(
          @Param("legalFirstName") String legalFirstName,
          @Param("legalLastName") String legalLastName,
          @Param("legalMiddleNames") String legalMiddleNames,
          @Param("usualFirstName") String usualFirstName,
          @Param("usualLastName") String usualLastName,
          @Param("usualMiddleNames") String usualMiddleNames);
}
