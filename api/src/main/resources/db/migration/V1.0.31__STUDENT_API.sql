UPDATE STUDENT_GENDER_CODE
SET EXPIRY_DATE = SYSDATE
WHERE GENDER_CODE in ('X','U');

UPDATE STUDENT_SEX_CODE
SET EXPIRY_DATE = SYSDATE
WHERE SEX_CODE in ('I','U');
