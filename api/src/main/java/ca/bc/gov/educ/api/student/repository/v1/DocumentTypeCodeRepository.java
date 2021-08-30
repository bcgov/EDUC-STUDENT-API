package ca.bc.gov.educ.api.student.repository.v1;

import ca.bc.gov.educ.api.student.model.v1.DocumentTypeCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeCodeRepository extends JpaRepository<DocumentTypeCodeEntity, String> {
}
