package ca.bc.gov.educ.api.student.support;

import ca.bc.gov.educ.api.student.service.CodeTableService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class MockConfiguration {
  @Bean
  @Primary
  public CodeTableService codeTableService() {
    return Mockito.mock(CodeTableService.class);
  }
}