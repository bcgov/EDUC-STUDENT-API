package ca.bc.gov.educ.api.student.health;

import io.nats.client.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class StudentAPICustomHealthCheckTest {

  @Autowired
  Connection natsConnection;

  @Autowired
  private StudentAPICustomHealthCheck studentAPICustomHealthCheck;


  @Test
  public void testGetHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
    assertThat(studentAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(studentAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(natsConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    assertThat(studentAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(studentAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.UP);
  }

}
