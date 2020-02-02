package ca.bc.gov.educ.api.student.struct;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class GenderCode {
  String genderCode;
  String label;
  String description;
  Integer displayOrder;
  Date effectiveDate;
  Date expiryDate;

}
