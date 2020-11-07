package ca.bc.gov.educ.api.student.util;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.struct.BaseRequest;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class RequestUtil {
  private RequestUtil() {
  }

  /**
   * set audit data to the object.
   *
   * @param baseRequest The object which will be persisted.
   */
  public static void setAuditColumns(@NotNull BaseRequest baseRequest, boolean isCreateOperation) {
    if (isCreateOperation && StringUtils.isBlank(baseRequest.getCreateUser())) {
      baseRequest.setCreateUser(ApplicationProperties.STUDENT_API);
    }
    if (StringUtils.isBlank(baseRequest.getUpdateUser())) {
      baseRequest.setUpdateUser(ApplicationProperties.STUDENT_API);
    }
    if(isCreateOperation) {
      baseRequest.setCreateDate(LocalDateTime.now().toString());
    }
    baseRequest.setUpdateDate(LocalDateTime.now().toString());
  }
}