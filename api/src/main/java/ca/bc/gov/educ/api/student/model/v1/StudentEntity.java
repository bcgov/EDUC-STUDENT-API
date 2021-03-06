package ca.bc.gov.educ.api.student.model.v1;

import ca.bc.gov.educ.api.student.util.UpperCase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Student entity.
 */
@Entity
@Table(name = "STUDENT")
@Data
@DynamicUpdate
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentEntity {
  /**
   * The Student id.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID studentID;
  /**
   * The Pen.
   */
  @NotNull(message = "pen cannot be null")
  @Column(name = "PEN")
  String pen;
  /**
   * The Legal first name.
   */
  @Column(name = "LEGAL_FIRST_NAME")
  @UpperCase
  String legalFirstName;
  /**
   * The Legal middle names.
   */
  @Column(name = "LEGAL_MIDDLE_NAMES")
  @UpperCase
  String legalMiddleNames;
  /**
   * The Legal last name.
   */
  @Column(name = "LEGAL_LAST_NAME")
  @UpperCase
  String legalLastName;
  /**
   * The Dob.
   */
  @Column(name = "DOB")
  @PastOrPresent
  LocalDate dob;
  /**
   * The Sex code.
   */
  @Column(name = "SEX_CODE")
  @UpperCase
  String sexCode;
  /**
   * The Gender code.
   */
  @Column(name = "GENDER_CODE")
  @UpperCase
  String genderCode;
  /**
   * The Usual first name.
   */
  @Column(name = "USUAL_FIRST_NAME")
  @UpperCase
  String usualFirstName;
  /**
   * The Usual middle names.
   */
  @Column(name = "USUAL_MIDDLE_NAMES")
  @UpperCase
  String usualMiddleNames;
  /**
   * The Usual last name.
   */
  @Column(name = "USUAL_LAST_NAME")
  @UpperCase
  String usualLastName;
  /**
   * The Email.
   */
  @Email(message = "Email must be valid email address")
  @Column(name = "EMAIL")
  String email;
  /**
   * The Email verified.
   */
  @NotNull(message = "Email verified cannot be null")
  @Column(name = "EMAIL_VERIFIED")
  @UpperCase
  String emailVerified;
  /**
   * The Deceased date.
   */
  @Column(name = "DECEASED_DATE")
  @PastOrPresent
  LocalDate deceasedDate;
  /**
   * The Postal code.
   */
  @Column(name = "POSTAL_CODE")
  @UpperCase
  String postalCode;
  /**
   * The Mincode.
   */
  @Column(name = "MINCODE")
  @UpperCase
  String mincode;
  /**
   * The Local id.
   */
  @Column(name = "LOCAL_ID")
  @UpperCase
  String localID;
  /**
   * The Grade code.
   */
  @Column(name = "GRADE_CODE")
  @UpperCase
  String gradeCode;
  /**
   * The Memo.
   */
  @Column(name = "MEMO")
  String memo;
  /**
   * The Grade year.
   */
  @Column(name = "GRADE_YEAR")
  String gradeYear;
  /**
   * The Demog code.
   */
  @Column(name = "DEMOG_CODE")
  @UpperCase
  String demogCode;
  /**
   * The True student id.
   */
  @Column(name = "TRUE_STUDENT_ID", columnDefinition = "BINARY(16)")
  UUID trueStudentID;
  /**
   * The Status code.
   */
  @Column(name = "STATUS_CODE")
  @UpperCase
  String statusCode;
  /**
   * The Create user.
   */
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;
  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE", updatable = false)
  @PastOrPresent
  LocalDateTime createDate;
  /**
   * The Update user.
   */
  @Column(name = "UPDATE_USER")
  String updateUser;
  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  @PastOrPresent
  LocalDateTime updateDate;
}
