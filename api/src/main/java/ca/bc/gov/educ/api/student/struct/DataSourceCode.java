package ca.bc.gov.educ.api.student.struct;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class DataSourceCode {
  String dataSourceCode;
  String label;
  String description;
  Integer displayOrder;
  Date effectiveDate;
  Date expiryDate;
}
