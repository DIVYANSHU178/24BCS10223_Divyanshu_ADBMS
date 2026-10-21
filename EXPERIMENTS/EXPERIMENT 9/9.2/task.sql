CREATE TABLE employee (
	emp_id INT PRIMARY KEY,
	emp_name VARCHAR(100) NOT NULL,
	per_hour_salary NUMERIC(10, 2) NOT NULL,
	working_hours INT NOT NULL,
	payable_amount NUMERIC(12, 2)
);

CREATE OR REPLACE FUNCTION calculate_payable_amount()
RETURNS TRIGGER AS $$
BEGIN
	NEW.payable_amount := NEW.per_hour_salary * NEW.working_hours;

	IF NEW.payable_amount > 25000 THEN
		RAISE EXCEPTION 'Payable amount cannot exceed 25000. Calculated amount: %',
			NEW.payable_amount;
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER employee_payable_amount_trigger
BEFORE INSERT OR UPDATE ON employee
FOR EACH ROW
EXECUTE FUNCTION calculate_payable_amount();

CREATE OR REPLACE FUNCTION display_success_message()
RETURNS TRIGGER AS $$
BEGIN
	RAISE NOTICE 'Rows Updated Successfully';
	RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER employee_statement_success_trigger
AFTER INSERT OR UPDATE ON employee
FOR EACH STATEMENT
EXECUTE FUNCTION display_success_message();

INSERT INTO employee (emp_id, emp_name, per_hour_salary, working_hours)
VALUES (101, 'Amit', 500, 40);

UPDATE employee
SET working_hours = 45
WHERE emp_id = 101;

DO $$
BEGIN
	INSERT INTO employee (emp_id, emp_name, per_hour_salary, working_hours)
	VALUES (102, 'Rahul', 700, 40);
EXCEPTION
	WHEN OTHERS THEN
		RAISE NOTICE '%', SQLERRM;
END;
$$;

DO $$
BEGIN
	UPDATE employee
	SET per_hour_salary = 1000
	WHERE emp_id = 101;
EXCEPTION
	WHEN OTHERS THEN
		RAISE NOTICE '%', SQLERRM;
END;
$$;

SELECT * FROM employee;
