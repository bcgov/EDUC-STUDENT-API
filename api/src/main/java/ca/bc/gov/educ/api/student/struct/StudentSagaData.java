package ca.bc.gov.educ.api.student.struct;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "studentSagaDataBuilder",buildMethodName = "studentSagaDataBuild")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentSagaData extends Student{
  private String sagaId;
}
