package ca.bc.gov.educ.api.student.struct;

import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseRequest {
  @Size(max = 32)
  public String createUser;
  @Size(max = 32)
  public String updateUser;
  @Null(message = "createDate should be null.")
  public String createDate;
  @Null(message = "updateDate should be null.")
  public String updateDate;
}