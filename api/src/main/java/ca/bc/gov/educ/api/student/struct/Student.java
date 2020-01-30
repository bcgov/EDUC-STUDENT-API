package ca.bc.gov.educ.api.student.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student implements Serializable {
  private static final long serialVersionUID = 1L;

  String studentID;
  String pen;
  String legalFirstName;
  String legalMiddleNames;
  String legalLastName;
  Date dob;
  String sexCode;
  String genderCode;
  String dataSourceCode;
  String usualFirstName;
  String usualMiddleNames;
  String usualLastName;
  @Email(message = "Email must be valid email address")
  String email;
  Date deceasedDate;
}
