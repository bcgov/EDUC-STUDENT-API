package ca.bc.gov.educ.api.student.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.student.model.StudentTwinReasonCodeEntity;

@Repository
public interface StudentTwinReasonCodeTableRepository extends CrudRepository<StudentTwinReasonCodeEntity, Long> {
    List<StudentTwinReasonCodeEntity> findAll();
}