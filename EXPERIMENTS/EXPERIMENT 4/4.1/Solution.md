
SELECT c.customer_name, o.order_date
FROM customers c
INNER JOIN orders o ON c.customer_id = o.customer_id;


SELECT c.customer_name, o.product_name
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id;

SELECT p.product_name, o.order_date
FROM products p
INNER JOIN orders o ON p.product_name = o.product_name;
