SELECT *
FROM Customer
LEFT JOIN Purchase
ON Customer.Customer_id = Purchase.Customer_id;