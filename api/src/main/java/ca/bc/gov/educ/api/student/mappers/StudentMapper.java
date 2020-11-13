package ca.bc.gov.educ.api.student.mappers;

import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.struct.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
@SuppressWarnings("squid:S1214")
public interface StudentMapper {

  StudentMapper mapper = Mappers.getMapper(StudentMapper.class);

  StudentEntity toModel(BaseStudent student);

  Student toStructure(StudentEntity studentEntity);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  SexCodeEntity toModel(SexCode structure);

  SexCode toStructure(SexCodeEntity entity);
  
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  GenderCodeEntity toModel(GenderCode structure);

  GenderCode toStructure(GenderCodeEntity entity);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  StatusCodeEntity toModel(StatusCode structure);

  StatusCode toStructure(StatusCodeEntity entity);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  DemogCodeEntity toModel(DemogCode structure);

  DemogCode toStructure(DemogCodeEntity entity);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  GradeCodeEntity toModel(GradeCode structure);

  GradeCode toStructure(GradeCodeEntity entity);
}
