package ca.bc.gov.educ.api.student.controller;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import ca.bc.gov.educ.api.student.properties.ApplicationProperties;
import ca.bc.gov.educ.api.student.struct.BaseRequest;

public abstract class BaseController {
  /**
   * set audit data to the object.
   *
   * @param baseRequest The object which will be persisted.
   */
  protected void setAuditColumns(@NotNull BaseRequest baseRequest) {
    if (StringUtils.isBlank(baseRequest.getCreateUser())) {
      baseRequest.setCreateUser(ApplicationProperties.STUDENT_API);
    }
    if (StringUtils.isBlank(baseRequest.getUpdateUser())) {
      baseRequest.setUpdateUser(ApplicationProperties.STUDENT_API);
    }
    baseRequest.setCreateDate(LocalDateTime.now().toString());
    baseRequest.setUpdateDate(LocalDateTime.now().toString());
  }
}