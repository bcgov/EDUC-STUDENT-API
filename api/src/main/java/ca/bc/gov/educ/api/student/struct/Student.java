package ca.bc.gov.educ.api.student.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student implements Serializable {
  private static final long serialVersionUID = 1L;

  String studentID;
  @NotNull(message = "PEN Number can not be null.")
  String pen;
  @Size(max = 40)
  @NotNull(message = "Legal First Name can not be null.")
  String legalFirstName;
  @Size(max = 60)
  String legalMiddleNames;
  @Size(max = 40)
  @NotNull(message = "Legal Last Name can not be null.")
  String legalLastName;
  @NotNull(message = "Date of Birth can not be null.")
  String dob;
  @NotNull(message = "Sex Code can not be null.")
  String sexCode;
  String genderCode;
  @Size(max = 40)
  String usualFirstName;
  @Size(max = 60)
  String usualMiddleNames;
  @Size(max = 40)
  String usualLastName;
  @Size(max = 80)
  @Email(message = "Email must be valid email address.")
  String email;
  String deceasedDate;
  @Size(max = 32)
  String createUser;
  @Size(max = 32)
  String updateUser;
  @Null(message = "createDate should be null.")
  String createDate;
  @Null(message = "updateDate should be null.")
  String updateDate;
}
