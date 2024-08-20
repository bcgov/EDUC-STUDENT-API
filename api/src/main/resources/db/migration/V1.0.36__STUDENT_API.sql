INSERT INTO STUDENT_SEX_CODE (SEX_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                                 CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('X', 'Gender Diverse',
        'Persons whose current gender is not exclusively as male or female. It includes people who do not have one gender, have no gender, are non-binary, or are Two-Spirit.',
        3, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
