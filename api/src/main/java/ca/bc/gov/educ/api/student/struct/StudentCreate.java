package ca.bc.gov.educ.api.student.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("java:S1948")
public class StudentCreate extends BaseStudent implements Serializable {
  private static final long serialVersionUID = 1L;

  List<StudentTwinAssociation> studentTwinAssociations;
  List<StudentMergeAssociation> studentMergeAssociations;
}
