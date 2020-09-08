package ca.bc.gov.educ.api.student.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class StudentRequestInterceptorTest {
  @Autowired
  private StudentRequestInterceptor requestInterceptor;

  @Test
  public void testPreHandle_givenRequest_shouldLogMessage() {
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn("get");
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));
    when(request.getQueryString()).thenReturn("pen=\"123456\"");
    assertTrue(requestInterceptor.preHandle(request, response, null));
  }

  @Test
  public void testAfterCompletion_givenSuccessResponse_shouldLogMessage() {
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    when(response.getStatus()).thenReturn(200);
    requestInterceptor.afterCompletion(request, response, null, null);
    verify(response, atMostOnce()).getStatus();
  }

  @Test
  public void testAfterCompletion_givenFailureResponse_shouldLogMessage() {
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    when(response.getStatus()).thenReturn(400);
    requestInterceptor.afterCompletion(request, response, null, null);
    verify(response, atMostOnce()).getStatus();
  }
}
