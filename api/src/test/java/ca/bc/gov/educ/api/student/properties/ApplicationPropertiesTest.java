package ca.bc.gov.educ.api.student.properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ApplicationPropertiesTest {

  @Autowired
  ApplicationProperties applicationProperties;

  @Test
  public void testGetStanUrl__shouldGiveValueFromProperties() {
    assertThat(applicationProperties).isNotNull();
    assertThat(applicationProperties.getNatsUrl()).isNotNull();
    assertThat(applicationProperties.getNatsUrl()).isEqualTo("test");
    assertThat(applicationProperties.getNatsMaxReconnect()).isEqualTo(60);
  }
}
