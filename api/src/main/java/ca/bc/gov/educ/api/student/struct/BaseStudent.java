package ca.bc.gov.educ.api.student.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseStudent extends BaseRequest {

  String studentID;
  @NotNull(message = "PEN Number can not be null.")
  String pen;
  @Size(max = 40)
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
  @NotNull(message = "Email verified cannot be null.")
  @Size(max = 1)
  @Pattern(regexp = "[YN]")
  String emailVerified;
  String deceasedDate;
  @Size(max = 7)
  @Pattern(regexp = "^([A-Z]\\d[A-Z]\\d[A-Z]\\d|)$")
  String postalCode;
  @Size(max = 8)
  String mincode;
  @Size(max = 12)
  String localID;
  @Size(max = 2)
  String gradeCode;
  @Size(max = 4)
  String gradeYear;
  @Size(max = 1)
  String demogCode;
  @Size(max = 1)
  String statusCode;
  @Size(max = 4000)
  String memo;
}
