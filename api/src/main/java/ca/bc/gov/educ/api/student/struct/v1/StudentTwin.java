package ca.bc.gov.educ.api.student.struct.v1;

import java.io.Serializable;

import javax.validation.constraints.*;

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
public class StudentTwin extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  String studentTwinID;
  @NotNull(message = "Student ID can not be null.")
  String studentID;
  @NotNull(message = "Twin Student ID can not be null.")
  String twinStudentID;
  @NotNull(message = "Student Twin Reason Code can not be null.")
  String studentTwinReasonCode;

  Student twinStudent;
}
