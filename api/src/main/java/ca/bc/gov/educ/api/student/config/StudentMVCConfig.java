package ca.bc.gov.educ.api.student.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StudentMVCConfig implements WebMvcConfigurer {

    @Getter(AccessLevel.PRIVATE)
    private final StudentRequestInterceptor studentRequestInterceptor;

    @Autowired
    public StudentMVCConfig(final StudentRequestInterceptor studentRequestInterceptor){
        this.studentRequestInterceptor = studentRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(studentRequestInterceptor).addPathPatterns("/**/**/");
    }
}
