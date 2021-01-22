package ca.bc.gov.educ.api.student.filter;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

/**
 * The type Student filter specs.
 */
@Service
@Slf4j
public class StudentFilterSpecs extends BaseFilterSpecs<StudentEntity> {

  /**
   * Instantiates a new Student filter specs.
   *
   * @param dateFilterSpecifications     the date filter specifications
   * @param dateTimeFilterSpecifications the date time filter specifications
   * @param integerFilterSpecifications  the integer filter specifications
   * @param stringFilterSpecifications   the string filter specifications
   * @param longFilterSpecifications     the long filter specifications
   * @param uuidFilterSpecifications     the uuid filter specifications
   * @param converters                   the converters
   */
  public StudentFilterSpecs(FilterSpecifications<StudentEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentEntity, String> stringFilterSpecifications, FilterSpecifications<StudentEntity, Long> longFilterSpecifications, FilterSpecifications<StudentEntity, UUID> uuidFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, converters);
  }
}
