package ca.bc.gov.educ.api.student.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

/**
 * The type Base request.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseRequest {
  /**
   * The Create user.
   */
  @Size(max = 32)
  public String createUser;
  /**
   * The Update user.
   */
  @Size(max = 32)
  public String updateUser;
  /**
   * The Create date.
   */
  @Null(message = "createDate should be null.")
  public String createDate;
  /**
   * The Update date.
   */
  @Null(message = "updateDate should be null.")
  public String updateDate;
}
