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