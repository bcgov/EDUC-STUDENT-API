package ca.bc.gov.educ.api.student.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Student history.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("java:S1948")
public class StudentHistory extends BaseStudent implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * The Student history id.
   */
  String studentHistoryID;
  /**
   * The History activity code.
   */
  String historyActivityCode;
}
