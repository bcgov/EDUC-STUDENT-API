package ca.bc.gov.educ.api.student.util;

import ca.bc.gov.educ.api.student.struct.v1.StudentMergeSourceCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jayway.jsonpath.JsonPath;

@RunWith(SpringRunner.class)
public class JsonUtilTest {

  @Test
  public void testGetJsonStringFromObject_WhenObjectIsValid_ShouldReturnJsonString() throws JSONException, JsonProcessingException {
    var jsonString = JsonUtil.getJsonStringFromObject(StudentMergeSourceCode.builder().mergeSourceCode("SCHOOL").build());
    String mergeSourceCode = JsonPath.parse(jsonString).read("$.mergeSourceCode");
    assertEquals("SCHOOL", mergeSourceCode);
  }

  @Test
  public void testGetJsonObjectFromString_WhenJsonStringIsValid_ShouldReturnObject() throws JSONException, JsonProcessingException {
    var object = JsonUtil.getJsonObjectFromString(StudentMergeSourceCode.class, "{\"mergeSourceCode\": \"SCHOOL\"}");
    assertEquals("SCHOOL", object.getMergeSourceCode());
  }
}
