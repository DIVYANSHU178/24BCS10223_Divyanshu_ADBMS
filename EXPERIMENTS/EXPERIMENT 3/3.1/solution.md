/* Write a query to count the number of students across departments who has scored more than 80 marks.*/

SELECT
    Department,
    COUNT(CASE
              WHEN Marks > 80 THEN 1
              ELSE NULL
         END) AS Dept_HighScore_Count
FROM student
GROUP BY Department;