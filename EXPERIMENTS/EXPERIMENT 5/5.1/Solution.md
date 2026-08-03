
SELECT ROUND((100*(SUM(CASE WHEN cuisine= 'American' THEN price ELSE 0 END))/SUM(price)),2) AS American_Revenue
FROM Orders;
