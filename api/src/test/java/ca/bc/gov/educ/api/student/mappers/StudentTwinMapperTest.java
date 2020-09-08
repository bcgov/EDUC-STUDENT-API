package ca.bc.gov.educ.api.student.mappers;

import ca.bc.gov.educ.api.student.struct.StudentTwinReasonCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(SpringRunner.class)
public class StudentTwinMapperTest {
  @Test
  public void testToModel_WhenStudentTwinReasonCode_ShouldReturnStudentTwinReasonCodeEntity() {
    var struct = StudentTwinReasonCode.builder().twinReasonCode("PENCREATE").label("Twinned by Creating PEN").effectiveDate("2020-01-01T01:01:01").expiryDate("2090-01-01T01:01:01").build();
    var entity = StudentTwinMapper.mapper.toModel(struct);
    assertEquals(struct.getTwinReasonCode(), entity.getTwinReasonCode());
    assertNull(entity.getCreateUser());
    assertNull(entity.getCreateDate());
    assertNull(entity.getUpdateUser());
    assertNull(entity.getUpdateDate());
  }
}
