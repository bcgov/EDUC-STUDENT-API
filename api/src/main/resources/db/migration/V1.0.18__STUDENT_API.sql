--Add Columns to STUDENT
ALTER TABLE STUDENT ADD (
    TRUE_STUDENT_ID RAW(16)
);

COMMENT ON COLUMN STUDENT.TRUE_STUDENT_ID IS 'The student ID which this student record is merged to.';