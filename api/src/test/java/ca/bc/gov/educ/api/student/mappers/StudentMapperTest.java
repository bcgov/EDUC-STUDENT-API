package ca.bc.gov.educ.api.student.mappers;

import ca.bc.gov.educ.api.student.struct.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(SpringRunner.class)
public class StudentMapperTest {
  @Test
  public void testToModel_WhenSexCode_ShouldReturnSexCodeEntity() {
    var struct = SexCode.builder().sexCode("M").label("Male").effectiveDate("2020-01-01T01:01:01").expiryDate("2090-01-01T01:01:01").build();
    var entity = StudentMapper.mapper.toModel(struct);
    assertEquals(struct.getSexCode(), entity.getSexCode());
    assertNull(entity.getCreateUser());
    assertNull(entity.getCreateDate());
    assertNull(entity.getUpdateUser());
    assertNull(entity.getUpdateDate());
  }

  @Test
  public void testToModel_WhenGenderCode_ShouldReturnGenderCodeEntity() {
    var struct = GenderCode.builder().genderCode("M").label("Male").effectiveDate("2020-01-01T01:01:01").expiryDate("2090-01-01T01:01:01").build();
    var entity = StudentMapper.mapper.toModel(struct);
    assertEquals(struct.getGenderCode(), entity.getGenderCode());
    assertNull(entity.getCreateUser());
    assertNull(entity.getCreateDate());
    assertNull(entity.getUpdateUser());
    assertNull(entity.getUpdateDate());
  }

  @Test
  public void testToModel_WhenStatusCode_ShouldReturnStatusCodeEntity() {
    var struct = StatusCode.builder().statusCode("A").label("Active").effectiveDate("2020-01-01T01:01:01").expiryDate("2090-01-01T01:01:01").build();
    var entity = StudentMapper.mapper.toModel(struct);
    assertEquals(struct.getStatusCode(), entity.getStatusCode());
    assertNull(entity.getCreateUser());
    assertNull(entity.getCreateDate());
    assertNull(entity.getUpdateUser());
    assertNull(entity.getUpdateDate());
  }

  @Test
  public void testToModel_WhenDemogCode_ShouldReturnDemogCodeEntity() {
    var struct = DemogCode.builder().demogCode("A").label("Accepted").effectiveDate("2020-01-01T01:01:01").expiryDate("2090-01-01T01:01:01").build();
    var entity = StudentMapper.mapper.toModel(struct);
    assertEquals(struct.getDemogCode(), entity.getDemogCode());
    assertNull(entity.getCreateUser());
    assertNull(entity.getCreateDate());
    assertNull(entity.getUpdateUser());
    assertNull(entity.getUpdateDate());
  }

  @Test
  public void testToModel_WhenGradeCode_ShouldReturnGradeCodeEntity() {
    var struct = GradeCode.builder().gradeCode("02").label("Grade 2").effectiveDate("2020-01-01T01:01:01").expiryDate("2090-01-01T01:01:01").build();
    var entity = StudentMapper.mapper.toModel(struct);
    assertEquals(struct.getGradeCode(), entity.getGradeCode());
    assertNull(entity.getCreateUser());
    assertNull(entity.getCreateDate());
    assertNull(entity.getUpdateUser());
    assertNull(entity.getUpdateDate());
  }
}
