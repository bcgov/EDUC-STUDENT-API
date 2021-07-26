package ca.bc.gov.educ.api.student.struct.v1;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * The type Base student.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public abstract class BaseStudent extends BaseRequest {

  /**
   * The Student id.
   */
  String studentID;
  /**
   * The Pen.
   */
  @NotNull(message = "PEN Number can not be null.")
  @Size(min = 9, max = 9)
  String pen;
  /**
   * The Legal first name.
   */
  @Size(max = 25)
  String legalFirstName;
  /**
   * The Legal middle names.
   */
  @Size(max = 25)
  String legalMiddleNames;
  /**
   * The Legal last name.
   */
  @Size(max = 25)
  @NotNull(message = "Legal Last Name can not be null.")
  String legalLastName;
  /**
   * The Dob.
   */
  @NotNull(message = "Date of Birth can not be null.")
  String dob;
  /**
   * The Sex code.
   */
  @NotNull(message = "Sex Code can not be null.")
  String sexCode;
  /**
   * The Gender code.
   */
  String genderCode;
  /**
   * The Usual first name.
   */
  @Size(max = 25)
  String usualFirstName;
  /**
   * The Usual middle names.
   */
  @Size(max = 25)
  String usualMiddleNames;
  /**
   * The Usual last name.
   */
  @Size(max = 25)
  String usualLastName;
  /**
   * The Email.
   */
  @Size(max = 80)
  @Email(message = "Email must be valid email address.")
  String email;
  /**
   * The Email verified.
   */
  @NotNull(message = "Email verified cannot be null.")
  @Size(max = 1)
  @Pattern(regexp = "[YN]")
  String emailVerified;
  /**
   * The Deceased date.
   */
  String deceasedDate;
  /**
   * The Postal code.
   */
  @Size(max = 7)
  String postalCode;
  /**
   * The Mincode.
   */
  @Size(max = 8)
  String mincode;
  /**
   * The Local id.
   */
  @Size(max = 12)
  String localID;
  /**
   * The Grade code.
   */
  @Size(max = 2)
  String gradeCode;
  /**
   * The Grade year.
   */
  @Size(max = 4)
  String gradeYear;
  /**
   * The Demog code.
   */
  @Size(max = 1)
  @NotNull
  String demogCode;
  /**
   * The Status code.
   */
  @Size(max = 1)
  @NotNull
  String statusCode;
  /**
   * The Memo.
   */
  @Size(max = 4000)
  String memo;
  /**
   * The True student id.
   */
  String trueStudentID;
}
