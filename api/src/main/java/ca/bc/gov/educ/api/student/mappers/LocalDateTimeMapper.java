package ca.bc.gov.educ.api.student.mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeMapper {

  public String map(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    dateTime = dateTime.withNano(0);
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
  }

  public LocalDateTime map(String dateTime) {
    if (dateTime == null) {
      return null;
    }
    return LocalDateTime.parse(dateTime).withNano(0);
  }
}
