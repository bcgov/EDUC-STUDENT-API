package ca.bc.gov.educ.api.student.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentMergeAssociation implements Serializable {
  private static final long serialVersionUID = 1L;
  @NotNull(message = "Merge Student ID can not be null.")
  String mergeStudentID;
  @NotNull(message = "Student Merge Direction Code can not be null.")
  String studentMergeDirectionCode;
  @NotNull(message = "Student Merge Source Code can not be null.")
  String studentMergeSourceCode;
}
