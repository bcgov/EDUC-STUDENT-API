package ca.bc.gov.educ.api.student.constant;

public enum CodeTableConstants {
  DATA_SOURCE_API_BASE_PATH("/datasource"),
  GENDER_CODE_API_BASE_PATH("/gender");

  private final String basePath;

  CodeTableConstants(final String basePath) {
    this.basePath = basePath;
  }

  public String getValue() {
    return this.basePath;
  }
}
