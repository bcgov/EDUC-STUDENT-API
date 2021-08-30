package ca.bc.gov.educ.api.student.mappers.v1;

import ca.bc.gov.educ.api.student.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.student.mappers.UUIDMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryActivityCodeEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistory;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistoryActivityCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Student history mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface StudentHistoryMapper {

  /**
   * The constant mapper.
   */
  StudentHistoryMapper mapper = Mappers.getMapper(StudentHistoryMapper.class);

  /**
   * To model student history activity code entity.
   *
   * @param structure the structure
   * @return the student history activity code entity
   */
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  StudentHistoryActivityCodeEntity toModel(StudentHistoryActivityCode structure);

  /**
   * To structure student history activity code.
   *
   * @param entity the entity
   * @return the student history activity code
   */
  StudentHistoryActivityCode toStructure(StudentHistoryActivityCodeEntity entity);

  /**
   * To model student history entity.
   *
   * @param studentHistory the student history
   * @return the student history entity
   */
  StudentHistoryEntity toModel(StudentHistory studentHistory);

  /**
   * To structure student history.
   *
   * @param studentHistoryEntity the student history entity
   * @return the student history
   */
  StudentHistory toStructure(StudentHistoryEntity studentHistoryEntity);
}
