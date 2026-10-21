# Experiment 9 - Task 2

Name: ABHINEET

UID: 24BCS10039

## Aim

To create row-level and statement-level triggers for an employee payroll management system in PostgreSQL.

## Question

Create an `employee` table with employee ID, employee name, per-hour salary, working hours, and payable amount. Create a row-level trigger that calculates `payable_amount` as `per_hour_salary * working_hours` whenever an employee is inserted or updated. Reject the operation if the calculated amount is greater than 25,000. Also create a statement-level trigger that displays `Rows Updated Successfully` after a successful `INSERT` or `UPDATE` statement.

## SQL Queries Used

```sql
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
```

## Output

```text
NOTICE:  Rows Updated Successfully
NOTICE:  Rows Updated Successfully
NOTICE:  Payable amount cannot exceed 25000. Calculated amount: 28000.00
NOTICE:  Payable amount cannot exceed 25000. Calculated amount: 45000.00

 emp_id | emp_name | per_hour_salary | working_hours | payable_amount
--------+----------+-----------------+---------------+----------------
    101 | Amit     |          500.00 |            45 |       22500.00
(1 row)
```

## Image Explanation

The row-level trigger calculates the payable amount before each insert or update. Amit's insert and update are allowed because the calculated amounts are within 25,000, and the statement-level trigger displays `Rows Updated Successfully` after each successful statement. The attempted insert for Rahul and update for Amit exceed the limit, so both operations are rejected with an exception.

## Result

The row-level trigger successfully calculated and validated payable amounts, while the statement-level trigger displayed the success message after valid insert and update statements. Operations exceeding 25,000 were rejected as required.