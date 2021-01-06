package ca.bc.gov.educ.api.student.repository.v1;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.student.model.v1.StudentTwinReasonCodeEntity;

@Repository
public interface StudentTwinReasonCodeTableRepository extends CrudRepository<StudentTwinReasonCodeEntity, Long> {
    List<StudentTwinReasonCodeEntity> findAll();
}
