package ca.bc.gov.educ.api.student.mappers.v1;

import ca.bc.gov.educ.api.student.mappers.UUIDMapper;
import ca.bc.gov.educ.api.student.model.v1.StudentTwinEntity;
import ca.bc.gov.educ.api.student.model.v1.StudentTwinReasonCodeEntity;
import ca.bc.gov.educ.api.student.struct.v1.StudentTwin;
import ca.bc.gov.educ.api.student.struct.v1.StudentTwinReasonCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, StudentMapper.class})
@SuppressWarnings("squid:S1214")
public interface StudentTwinMapper {

  StudentTwinMapper mapper = Mappers.getMapper(StudentTwinMapper.class);

  StudentTwinEntity toModel(StudentTwin studentTwin);

  StudentTwin toStructure(StudentTwinEntity studentTwinEntity);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  StudentTwinReasonCodeEntity toModel(StudentTwinReasonCode structure);

  StudentTwinReasonCode toStructure(StudentTwinReasonCodeEntity entity);
}
