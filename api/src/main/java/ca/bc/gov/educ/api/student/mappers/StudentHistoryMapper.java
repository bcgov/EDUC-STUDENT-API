package ca.bc.gov.educ.api.student.mappers;

import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.struct.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
@SuppressWarnings("squid:S1214")
public interface StudentHistoryMapper {

  StudentHistoryMapper mapper = Mappers.getMapper(StudentHistoryMapper.class);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  StudentHistoryActivityCodeEntity toModel(StudentHistoryActivityCode structure);

  StudentHistoryActivityCode toStructure(StudentHistoryActivityCodeEntity entity);

  StudentHistoryEntity toModel(StudentHistory studentHistory);

  StudentHistory toStructure(StudentHistoryEntity studentHistoryEntity);
}
