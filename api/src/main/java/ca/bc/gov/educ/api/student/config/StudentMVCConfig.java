package ca.bc.gov.educ.api.student.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Student mvc config.
 */
@Configuration
public class StudentMVCConfig implements WebMvcConfigurer {

  @Getter(AccessLevel.PRIVATE)
  private final StudentRequestInterceptor studentRequestInterceptor;

  /**
   * Instantiates a new Student mvc config.
   *
   * @param studentRequestInterceptor the student request interceptor
   */
  @Autowired
  public StudentMVCConfig(final StudentRequestInterceptor studentRequestInterceptor) {
    this.studentRequestInterceptor = studentRequestInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(studentRequestInterceptor).addPathPatterns("/**");
  }
}
