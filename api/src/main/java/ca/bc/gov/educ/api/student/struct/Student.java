package ca.bc.gov.educ.api.student.struct;

import java.io.Serializable;

import javax.validation.constraints.*;

import ca.bc.gov.educ.api.student.util.UpperCase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  String studentID;
  @NotNull(message = "PEN Number can not be null.")
  String pen;
  @Size(max = 40)
  @NotNull(message = "Legal First Name can not be null.")
  @UpperCase
  String legalFirstName;
  @Size(max = 60)
  @UpperCase
  String legalMiddleNames;
  @Size(max = 40)
  @NotNull(message = "Legal Last Name can not be null.")
  @UpperCase
  String legalLastName;
  @NotNull(message = "Date of Birth can not be null.")
  String dob;
  @NotNull(message = "Sex Code can not be null.")
  @UpperCase
  String sexCode;
  @UpperCase
  String genderCode;
  @Size(max = 40)
  @UpperCase
  String usualFirstName;
  @Size(max = 60)
  @UpperCase
  String usualMiddleNames;
  @Size(max = 40)
  @UpperCase
  String usualLastName;
  @Size(max = 80)
  @Email(message = "Email must be valid email address.")
  String email;
  @NotNull(message = "Email verified cannot be null.")
  @Size(max = 1)
  @Pattern(regexp = "[YN]")
  @UpperCase
  String emailVerified;
  String deceasedDate;
  @Size(max = 7)
  @Pattern(regexp = "^([A-Z]\\d[A-Z]\\d[A-Z]\\d|)$")
  String postalCode;
  @Size(max = 8)
  @UpperCase
  String mincode;
  @Size(max = 12)
  @UpperCase
  String localID;
  @Size(max = 2)
  @UpperCase
  String gradeCode;
  @Size(max = 4)
  String gradeYear;
  @Size(max = 1)
  @UpperCase
  String demogCode;
  @Size(max = 1)
  @UpperCase
  String statusCode;
  @Size(max = 25)
  String memo;
}
