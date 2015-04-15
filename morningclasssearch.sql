SELECT course_id, semester, year, sec_id
FROM section NATURAL JOIN time_slot 
WHERE start_hr <= 12
GROUP BY course_id