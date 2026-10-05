CREATE TABLE Customers (
    customer_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    email VARCHAR(100),
    city VARCHAR(50),
    contact_no VARCHAR(20)
);

CREATE TABLE Orders (
    order_id INT PRIMARY KEY,
    customer_id INT,
    order_date DATE,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

INSERT INTO Customers (customer_id, customer_name, email, city, contact_no) VALUES
(101, 'Amit Sharma', 'amit@gmail.com', 'Delhi', '9876543210'),
(102, 'Rahul Verma', 'rahul@gmail.com', 'Mumbai', '9876543211'),
(103, 'Priya Singh', 'priya@gmail.com', 'Delhi', '9876543212'),
(104, 'Neha Kapoor', 'neha@gmail.com', 'Pune', '9876543213');

INSERT INTO Orders (order_id, customer_id, order_date) VALUES
(1, 101, '2024-01-10'),
(2, 101, '2024-01-25'),
(3, 103, '2024-01-15');

SELECT c.customer_id, c.customer_name, c.email, c.city, c.contact_no
FROM Customers c
JOIN Orders o ON c.customer_id = o.customer_id
WHERE o.order_date >= '2024-01-01' AND o.order_date < '2024-02-01'
GROUP BY c.customer_id, c.customer_name, c.email, c.city, c.contact_no
HAVING COUNT(o.order_id) > 1
ORDER BY c.customer_id ASC;