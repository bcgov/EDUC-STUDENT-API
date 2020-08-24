package ca.bc.gov.educ.api.student.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.student.model.StudentMergeDirectionCodeEntity;

@Repository
public interface StudentMergeDirectionCodeTableRepository extends CrudRepository<StudentMergeDirectionCodeEntity, Long> {
    List<StudentMergeDirectionCodeEntity> findAll();
}
