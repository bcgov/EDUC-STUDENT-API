--Modify Columns in STUDENT to make postal field 6
ALTER TABLE STUDENT
    MODIFY (
        POSTAL_CODE VARCHAR2(7)
        );
