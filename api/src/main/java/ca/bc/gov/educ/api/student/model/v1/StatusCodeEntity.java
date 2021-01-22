package ca.bc.gov.educ.api.student.model.v1;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

/**
 * The type Status code entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STUDENT_STATUS_CODE")
public class StatusCodeEntity {

  /**
   * The Status code.
   */
  @Id
  @Column(name = "STATUS_CODE", unique = true, updatable = false)
  String statusCode;

  /**
   * The Label.
   */
  @NotNull(message = "label cannot be null")
  @Column(name = "LABEL")
  String label;

  /**
   * The Description.
   */
  @NotNull(message = "description cannot be null")
  @Column(name = "DESCRIPTION")
  String description;

  /**
   * The Display order.
   */
  @NotNull(message = "displayOrder cannot be null")
  @Column(name = "DISPLAY_ORDER")
  Integer displayOrder;

  /**
   * The Effective date.
   */
  @NotNull(message = "effectiveDate cannot be null")
  @Column(name = "EFFECTIVE_DATE")
  LocalDateTime effectiveDate;

  /**
   * The Expiry date.
   */
  @NotNull(message = "expiryDate cannot be null")
  @Column(name = "EXPIRY_DATE")
  LocalDateTime expiryDate;

  /**
   * The Create user.
   */
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  /**
   * The Create date.
   */
  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  /**
   * The Update user.
   */
  @Column(name = "UPDATE_USER", updatable = false)
  String updateUser;

  /**
   * The Update date.
   */
  @PastOrPresent
  @Column(name = "UPDATE_DATE", updatable = false)
  LocalDateTime updateDate;
}
