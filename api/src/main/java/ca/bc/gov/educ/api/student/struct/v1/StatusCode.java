package ca.bc.gov.educ.api.student.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Status code.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("squid:S1700")
public class StatusCode implements Serializable {
  /**
   * The Status code.
   */
  String statusCode;
  /**
   * The Label.
   */
  String label;
  /**
   * The Description.
   */
  String description;
  /**
   * The Display order.
   */
  Integer displayOrder;
  /**
   * The Effective date.
   */
  String effectiveDate;
  /**
   * The Expiry date.
   */
  String expiryDate;
}
