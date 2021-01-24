package ca.bc.gov.educ.api.student.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
public class ApplicationProperties {

  /**
   * The constant STUDENT_API.
   */
  public static final String STUDENT_API = "STUDENT-API";

  /**
   * The Stan url.
   */
  @Value("${stan.url}")
  String stanUrl;

  /**
   * The Stan cluster.
   */
  @Value("${stan.cluster}")
  String stanCluster;
}
