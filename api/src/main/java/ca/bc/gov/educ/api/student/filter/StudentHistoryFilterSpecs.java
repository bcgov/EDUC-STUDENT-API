package ca.bc.gov.educ.api.student.filter;

import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

/**
 * The type Student history filter specs.
 */
@Service
@Slf4j
public class StudentHistoryFilterSpecs extends BaseFilterSpecs<StudentHistoryEntity> {

  /**
   * Instantiates a new Student history filter specs.
   *
   * @param dateFilterSpecifications     the date filter specifications
   * @param dateTimeFilterSpecifications the date time filter specifications
   * @param integerFilterSpecifications  the integer filter specifications
   * @param stringFilterSpecifications   the string filter specifications
   * @param longFilterSpecifications     the long filter specifications
   * @param uuidFilterSpecifications     the uuid filter specifications
   * @param converters                   the converters
   */
  public StudentHistoryFilterSpecs(FilterSpecifications<StudentHistoryEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentHistoryEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentHistoryEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentHistoryEntity, String> stringFilterSpecifications, FilterSpecifications<StudentHistoryEntity, Long> longFilterSpecifications, FilterSpecifications<StudentHistoryEntity, UUID> uuidFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, converters);
  }
}
