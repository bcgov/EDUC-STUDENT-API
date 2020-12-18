package ca.bc.gov.educ.api.student.filter;

import ca.bc.gov.educ.api.student.model.StudentHistoryEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class StudentHistoryFilterSpecs extends BaseFilterSpecs<StudentHistoryEntity>{

  public StudentHistoryFilterSpecs(FilterSpecifications<StudentHistoryEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentHistoryEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentHistoryEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentHistoryEntity, String> stringFilterSpecifications, FilterSpecifications<StudentHistoryEntity, Long> longFilterSpecifications, FilterSpecifications<StudentHistoryEntity, UUID> uuidFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications,dateTimeFilterSpecifications,integerFilterSpecifications,stringFilterSpecifications,longFilterSpecifications,uuidFilterSpecifications,converters);
  }
}
