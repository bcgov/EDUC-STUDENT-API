package ca.bc.gov.educ.api.student.mappers.v1;

import ca.bc.gov.educ.api.student.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.student.mappers.StringMapper;
import ca.bc.gov.educ.api.student.mappers.UUIDMapper;
import ca.bc.gov.educ.api.student.model.v1.*;
import ca.bc.gov.educ.api.student.struct.v1.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Student mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
@SuppressWarnings("squid:S1214")
public interface StudentMapper {

  /**
   * The constant mapper.
   */
  StudentMapper mapper = Mappers.getMapper(StudentMapper.class);

  /**
   * To model student entity.
   *
   * @param student the student
   * @return the student entity
   */
  StudentEntity toModel(BaseStudent student);

  /**
   * To model student entity.
   *
   * @param student the student
   * @return the student entity
   */
  @Mapping(target = "demogCode", source = "demogCode", defaultValue = "A")
  StudentEntity toModel(StudentCreate student);

  /**
   * To structure student.
   *
   * @param studentEntity the student entity
   * @return the student
   */
  Student toStructure(StudentEntity studentEntity);


  /**
   * To structure student create.
   *
   * @param studentEntity the student entity
   * @return the student
   */
  StudentCreate toStructure(StudentEntity studentEntity, String historyActivityCode);

  StudentUpdate toStudentUpdateStruct(StudentEntity studentEntity, String historyActivityCode);

  /**
   * To model sex code entity.
   *
   * @param structure the structure
   * @return the sex code entity
   */
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  SexCodeEntity toModel(SexCode structure);

  /**
   * To structure sex code.
   *
   * @param entity the entity
   * @return the sex code
   */
  SexCode toStructure(SexCodeEntity entity);

  /**
   * To model gender code entity.
   *
   * @param structure the structure
   * @return the gender code entity
   */
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  GenderCodeEntity toModel(GenderCode structure);

  /**
   * To structure gender code.
   *
   * @param entity the entity
   * @return the gender code
   */
  GenderCode toStructure(GenderCodeEntity entity);

  /**
   * To model status code entity.
   *
   * @param structure the structure
   * @return the status code entity
   */
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  StatusCodeEntity toModel(StatusCode structure);

  /**
   * To structure status code.
   *
   * @param entity the entity
   * @return the status code
   */
  StatusCode toStructure(StatusCodeEntity entity);

  /**
   * To model demog code entity.
   *
   * @param structure the structure
   * @return the demog code entity
   */
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  DemogCodeEntity toModel(DemogCode structure);

  /**
   * To structure demog code.
   *
   * @param entity the entity
   * @return the demog code
   */
  DemogCode toStructure(DemogCodeEntity entity);

  /**
   * To model grade code entity.
   *
   * @param structure the structure
   * @return the grade code entity
   */
  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  GradeCodeEntity toModel(GradeCode structure);

  /**
   * To structure grade code.
   *
   * @param entity the entity
   * @return the grade code
   */
  GradeCode toStructure(GradeCodeEntity entity);

  DocTypeCode toStructure(DocumentTypeCodeEntity entity);

  @Mapping(target = "updateUser", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "createUser", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  DocumentTypeCodeEntity toModel(DocTypeCode struct);
}
