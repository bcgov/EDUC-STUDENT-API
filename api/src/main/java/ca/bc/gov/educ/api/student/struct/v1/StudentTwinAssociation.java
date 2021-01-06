package ca.bc.gov.educ.api.student.struct.v1;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentTwinAssociation implements Serializable {
  private static final long serialVersionUID = 1L;

  @NotNull(message = "Twin Student ID can not be null.")
  String twinStudentID;
  @NotNull(message = "Student Twin Reason Code can not be null.")
  String studentTwinReasonCode;
}
