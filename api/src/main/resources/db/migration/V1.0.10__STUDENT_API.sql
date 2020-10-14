-- Student grade codes
UPDATE STUDENT_GRADE_CODE
SET    LABEL = 'Secondary Ungraded', DESCRIPTION = 'Secondary ungraded'
WHERE  GRADE_CODE = 'SU';

UPDATE STUDENT_GRADE_CODE
SET    LABEL = 'Elementary Ungraded', DESCRIPTION = 'Elementary ungraded'
WHERE  GRADE_CODE = 'EU';