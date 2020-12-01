package ca.bc.gov.educ.api.student.struct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameVariant implements Serializable {
  private static final long serialVersionUID = 1L;

  List<String> legalFirstNames;
  List<String> legalLastNames;
  List<String> legalMiddleNames;

  List<String> usualFirstNames;
  List<String> usualLastNames;
  List<String> usualMiddleNames;
}

