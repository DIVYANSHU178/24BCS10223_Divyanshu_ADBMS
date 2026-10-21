# Experiment 9 - Task 1

Name: ABHINEET

UID: 24BCS10039

## Aim

To create a row-level `BEFORE UPDATE` trigger that restricts a salary increase to a maximum of 15% of the old salary.

## Question

Implement a row-level `BEFORE UPDATE` trigger on the `Salary_Hike` table. If the new salary exceeds 15% of the old salary, raise a user-defined exception with an appropriate error message.

## SQL Queries Used

```sql
CREATE TABLE Salary_Hike (
    employee_id NUMBER PRIMARY KEY,
    employee_name VARCHAR2(100),
    salary NUMBER(10, 2)
);

INSERT INTO Salary_Hike VALUES (101, 'Amit', 50000);
INSERT INTO Salary_Hike VALUES (102, 'Rahul', 60000);

CREATE OR REPLACE TRIGGER Salary_Hike_Trigger
BEFORE UPDATE OF salary ON Salary_Hike
FOR EACH ROW
DECLARE
    salary_limit_exceeded EXCEPTION;
BEGIN
    IF :NEW.salary > :OLD.salary * 1.15 THEN
        RAISE salary_limit_exceeded;
    END IF;
EXCEPTION
    WHEN salary_limit_exceeded THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'Salary increase cannot exceed 15% of the old salary.'
        );
END;
/

UPDATE Salary_Hike
SET salary = 57000
WHERE employee_id = 101;

BEGIN
    UPDATE Salary_Hike
    SET salary = 70000
    WHERE employee_id = 102;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE(SQLERRM);
END;
/

SELECT * FROM Salary_Hike;
```

## Output

```text
Table created.
2 rows inserted.
Trigger created.

1 row updated.

ORA-20001: Salary increase cannot exceed 15% of the old salary.

EMPLOYEE_ID EMPLOYEE_NAME SALARY
----------- ------------- ------
101         Amit          57000
102         Rahul         60000
```

## Image Explanation

The trigger allows Amit's salary to increase from 50000 to 57000 because the increase is 14%, which is within the 15% limit. Rahul's attempted increase from 60000 to 70000 exceeds the limit, so the user-defined exception prevents the update and displays the custom error message.

## Result

The row-level `BEFORE UPDATE` trigger was successfully created. It allowed valid salary increases and rejected increases greater than 15% of the old salary.