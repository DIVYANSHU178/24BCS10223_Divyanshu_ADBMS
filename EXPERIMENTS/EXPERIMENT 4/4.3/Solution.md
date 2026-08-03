
SELECT c.customer_name, o.*
FROM customers c
RIGHT JOIN orders o ON c.customer_id = o.customer_id;

SELECT p.product_name, c.category_name
FROM products p
FULL OUTER JOIN categories c ON p.category_id = c.category_id;


select c.category_name, p.product_name, p.price
from products p
RIGHT JOIN categories c
on p.category_id = c.category_id;
