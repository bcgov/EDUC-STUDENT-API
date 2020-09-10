package ca.bc.gov.educ.api.student.struct;

import ca.bc.gov.educ.api.student.filter.FilterOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchCriteria {
  @NotNull
  String key;
  @NotNull
  FilterOperation operation;
  String value;
  @NotNull
  ValueType valueType;
  /**
   * The Condition. ENUM to hold and AND OR
   */
  Condition condition;
}
