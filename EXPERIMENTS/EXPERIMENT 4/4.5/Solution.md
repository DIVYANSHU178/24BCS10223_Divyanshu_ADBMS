
SELECT e1.employee_name AS Employee, e2.employee_name AS Manager
FROM employees e1
LEFT JOIN employees e2 ON e1.manager_id = e2.employee_id;


SELECT c.customer_name, p.product_name
FROM customers c
CROSS JOIN products p;
