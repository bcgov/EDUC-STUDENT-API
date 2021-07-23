--Modify Columns in STUDENT to make postal field 7
ALTER TABLE API_STUDENT.STUDENT
    MODIFY (
        POSTAL_CODE VARCHAR2(7)
        );

--Modify Columns in STUDENT to make postal field 7
ALTER TABLE API_STUDENT.STUDENT_HISTORY
    MODIFY (
        POSTAL_CODE VARCHAR2(7)
        );
