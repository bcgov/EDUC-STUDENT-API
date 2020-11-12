package ca.bc.gov.educ.api.student.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("java:S1948")
public class StudentUpdate extends BaseStudent implements Serializable {
  private static final long serialVersionUID = 1L;
  @NotNull(message = "historyActivityCode can not be null.")
  String historyActivityCode;
}