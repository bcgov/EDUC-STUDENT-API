--Modify Columns in STUDENT_HISTORY to lengthen memo field
ALTER TABLE STUDENT_HISTORY MODIFY (
     MEMO VARCHAR2(4000)
);
