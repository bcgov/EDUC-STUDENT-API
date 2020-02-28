package ca.bc.gov.educ.api.student.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
@Setter
public class ApplicationProperties {

  public static final String STUDENT_API = "STUDENT-API";
  @Value("${client.id}")
  private String clientID;
  @Value("${client.secret}")
  private String clientSecret;
  @Value("${token.url}")
  private String tokenURL;
  @Value("${codetable.api.url}")
  private String codetableApiURL;

}
