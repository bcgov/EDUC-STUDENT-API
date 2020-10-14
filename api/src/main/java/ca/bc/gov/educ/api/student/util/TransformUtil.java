package ca.bc.gov.educ.api.student.util;

import ca.bc.gov.educ.api.student.exception.StudentRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransformUtil {
  public static <T> T uppercaseFields(T record) {
    Class<?> clazz = record.getClass();
    List<Field> fields = new ArrayList<>();
    Class<?> superClazz = clazz;
    while (!superClazz.equals(Object.class)) {
      fields.addAll(Arrays.asList(superClazz.getDeclaredFields()));
      superClazz = superClazz.getSuperclass();
    }
    fields.forEach(field -> TransformUtil.transformFieldToUppercase(field, record));
    return record;
  }

  private static <T> void transformFieldToUppercase(Field field, T record) {
    if (!field.getType().equals(String.class)) {
      return;
    }

    if (field.getAnnotation(UpperCase.class) != null) {
      try {
        var unsetAccessible = false;
        if (!field.canAccess(record)) {
          unsetAccessible = true;
          field.setAccessible(true);
        }
        String entityFieldValue = (String) field.get(record);
        if (entityFieldValue != null) {
          field.set(record, entityFieldValue.toUpperCase());
        }
        if (unsetAccessible) {
          field.setAccessible(false);
        }
      } catch (IllegalAccessException ex) {
        throw new StudentRuntimeException(ex.getMessage());
      }
    }

  }
}
