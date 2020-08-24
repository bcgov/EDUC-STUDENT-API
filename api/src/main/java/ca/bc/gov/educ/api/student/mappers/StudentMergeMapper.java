package ca.bc.gov.educ.api.student.mappers;

import ca.bc.gov.educ.api.student.model.*;
import ca.bc.gov.educ.api.student.struct.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, StudentMapper.class})
@SuppressWarnings("squid:S1214")
public interface StudentMergeMapper {

  StudentMergeMapper mapper = Mappers.getMapper(StudentMergeMapper.class);

  StudentMergeEntity toModel(StudentMerge studentMerge);

  StudentMerge toStructure(StudentMergeEntity studentMergeEntity);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  StudentMergeSourceCodeEntity toModel(StudentMergeSourceCode structure);

  StudentMergeSourceCode toStructure(StudentMergeSourceCodeEntity entity);
}
