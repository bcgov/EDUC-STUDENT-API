package ca.bc.gov.educ.api.student.filter;

import ca.bc.gov.educ.api.student.model.v1.StudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class StudentFilterSpecs extends BaseFilterSpecs<StudentEntity>{

  public StudentFilterSpecs(FilterSpecifications<StudentEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentEntity, String> stringFilterSpecifications, FilterSpecifications<StudentEntity, Long> longFilterSpecifications, FilterSpecifications<StudentEntity, UUID> uuidFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications,dateTimeFilterSpecifications,integerFilterSpecifications,stringFilterSpecifications,longFilterSpecifications,uuidFilterSpecifications,converters);
  }
}
