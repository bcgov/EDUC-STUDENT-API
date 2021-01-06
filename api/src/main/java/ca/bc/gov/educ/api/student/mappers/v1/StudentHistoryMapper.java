package ca.bc.gov.educ.api.student.mappers.v1;

import ca.bc.gov.educ.api.student.mappers.UUIDMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryActivityCodeEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentHistoryEntity;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistory;
import ca.bc.gov.educ.api.student.struct.v1.StudentHistoryActivityCode;
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
