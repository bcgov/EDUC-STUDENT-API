--Add Columns to STUDENT_HISTORY
ALTER TABLE STUDENT_HISTORY ADD (
    TRUE_STUDENT_ID RAW(16)
);

COMMENT ON COLUMN STUDENT_HISTORY.TRUE_STUDENT_ID IS 'The student ID which this student record is merged to.';