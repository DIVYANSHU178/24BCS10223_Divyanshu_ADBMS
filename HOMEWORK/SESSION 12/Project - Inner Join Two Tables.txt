SELECT *
FROM Customer
JOIN Purchase
ON Customer.Customer_id = Purchase.Customer_id;