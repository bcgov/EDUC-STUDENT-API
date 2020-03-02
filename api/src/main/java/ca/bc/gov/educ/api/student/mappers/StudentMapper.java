package ca.bc.gov.educ.api.student.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ca.bc.gov.educ.api.student.model.GenderCodeEntity;
import ca.bc.gov.educ.api.student.model.SexCodeEntity;
import ca.bc.gov.educ.api.student.model.StudentEntity;
import ca.bc.gov.educ.api.student.struct.GenderCode;
import ca.bc.gov.educ.api.student.struct.SexCode;
import ca.bc.gov.educ.api.student.struct.Student;

@Mapper(uses = UUIDMapper.class)
@SuppressWarnings("squid:S1214")
public interface StudentMapper {

  StudentMapper mapper = Mappers.getMapper(StudentMapper.class);

  StudentEntity toModel(Student student);

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
}
