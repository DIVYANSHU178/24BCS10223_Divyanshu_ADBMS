
SELECT *
FROM student
JOIN course
ON student.Course_id = course.Course_id;

SELECT *
FROM student
LEFT JOIN course
ON student.Course_id = course.Course_id;
