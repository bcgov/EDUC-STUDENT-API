package ca.bc.gov.educ.api.student.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("squid:S1700")
public class StudentHistoryActivityCode {
  String historyActivityCode;
  String label;
  String description;
  Integer displayOrder;
  String effectiveDate;
  String expiryDate;

}
