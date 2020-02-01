package ca.bc.gov.educ.api.student.struct;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class DataSourceCode extends BaseCodeTableData implements Serializable {
  String dataSourceCode;
  String label;
  String description;
  Integer displayOrder;
  Date effectiveDate;
  Date expiryDate;
}
