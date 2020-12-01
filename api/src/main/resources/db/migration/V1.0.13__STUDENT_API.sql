--Modify Columns in STUDENT to lengthen memo field
ALTER TABLE STUDENT MODIFY (
     MEMO VARCHAR2(4000)
);