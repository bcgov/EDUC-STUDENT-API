package ca.bc.gov.educ.api.student.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.Data;

@Entity
@Table(name = "student")
@Data
public class StudentEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "student_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID studentID;
  @NotNull(message = "pen cannot be null")
  @Column(name = "pen")
  String pen;
  @Column(name = "legal_first_name")
  String legalFirstName;
  @Column(name = "legal_middle_names")
  String legalMiddleNames;
  @Column(name = "legal_last_name")
  String legalLastName;
  @Column(name = "dob")
  @PastOrPresent
  LocalDate dob;
  @Column(name = "sex_code")
  String sexCode;
  @Column(name = "gender_code")
  String genderCode;
  @Column(name = "usual_first_name")
  String usualFirstName;
  @Column(name = "usual_middle_names")
  String usualMiddleNames;
  @Column(name = "usual_last_name")
  String usualLastName;
  @Email(message = "Email must be valid email address")
  @Column(name = "email")
  String email;
  @Column(name = "deceased_date")
  @PastOrPresent
  LocalDate deceasedDate;
  @Column(name = "create_user", updatable = false)
  String createUser;
  @Column(name = "create_date", updatable = false)
  @PastOrPresent
  LocalDateTime createDate;
  @Column(name = "update_user")
  String updateUser;
  @Column(name = "update_date")
  @PastOrPresent
  LocalDateTime updateDate;

}
