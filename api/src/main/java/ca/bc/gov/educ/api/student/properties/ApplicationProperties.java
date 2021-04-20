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
  public static final String STREAM_NAME="STUDENT_EVENTS";
  /**
   * The Stan url.
   */
  @Value("${stan.url}")
  String natsUrl;


  @Value("${nats.maxReconnect}")
  Integer natsMaxReconnect;
}
