package ca.bc.gov.educ.api.student.struct;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenderCode extends BaseCodeTableData implements Serializable {
  String genderCode;
  String label;
  String description;
  Integer displayOrder;
  Date effectiveDate;
  Date expiryDate;

}
