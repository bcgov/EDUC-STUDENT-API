package ca.bc.gov.educ.api.student.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("squid:S1700")
public class StudentMergeSourceCode implements Serializable {
  private static final long serialVersionUID = 1L;
  String mergeSourceCode;
  String label;
  String description;
  Integer displayOrder;
  String effectiveDate;
  String expiryDate;
}
