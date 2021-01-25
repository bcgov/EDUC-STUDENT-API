package ca.bc.gov.educ.api.student.health;

import ca.bc.gov.educ.api.student.messaging.NatsConnection;
import io.nats.client.Connection;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class StudentAPICustomHealthCheck implements HealthIndicator {
  private final NatsConnection natsConnection;

  public StudentAPICustomHealthCheck(NatsConnection natsConnection) {
    this.natsConnection = natsConnection;
  }

  @Override
  public Health getHealth(boolean includeDetails) {
    return healthCheck();
  }


  @Override
  public Health health() {
    return healthCheck();
  }

  private Health healthCheck() {
    if (this.natsConnection == null) {
      return Health.down().withDetail("NATS", " Connection object is missing.").build();
    } else if (this.natsConnection.getNatsCon() == null) {
      return Health.down().withDetail("NATS", " Connection is null.").build();
    } else if (this.natsConnection.getNatsCon().getStatus() == Connection.Status.CLOSED) {
      return Health.down().withDetail("NATS", " Connection is Closed.").build();
    }
    return Health.up().build();
  }
}
