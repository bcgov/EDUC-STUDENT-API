package ca.bc.gov.educ.api.student.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Setter
@Getter
public class ApplicationProperties {

  /**
   * The constant STUDENT_API.
   */
  public static final String STUDENT_API = "STUDENT-API";

  @Value("${stan.url}")
  private String stanUrl;

  @Value("${stan.cluster}")
  private String stanCluster;
}
