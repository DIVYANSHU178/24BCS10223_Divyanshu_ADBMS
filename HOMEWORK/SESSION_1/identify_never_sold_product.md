SELECT product_id, product_name FROM inventory_current_stock WHERE product_id NOT IN (SELECT product_id from sales_transactions);
