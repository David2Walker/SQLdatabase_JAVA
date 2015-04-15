SELECT ID, student.name, CASE
  WHEN grade = "A"
  THEN sum(4*credits)/sum(credits)
  WHEN grade = "A-"
  THEN sum(3.67*credits)/sum(credits)
   WHEN grade = "B+"
  THEN sum(3.33*credits)/sum(credits)
   WHEN grade = "B"
  THEN sum(3*credits)/sum(credits)
   WHEN grade = "B-"
  THEN sum(2.67*credits)/sum(credits)
   WHEN grade = "C+"
  THEN sum(2.33*credits)/sum(credits)
   WHEN grade = "C"
  THEN sum(2*credits)/sum(credits)
   WHEN grade = "C-"
  THEN sum(1.67*credits)/sum(credits)
   WHEN grade = "D+"
  THEN sum(1.33*credits)/sum(credits)
   WHEN grade = "D"
  THEN sum(1*credits)/sum(credits)
   WHEN grade = "D-"
  THEN sum(.67*credits)/sum(credits)
   WHEN grade = "F"
  THEN 0
  ELSE 0 
END "gpa"
FROM student NATURAL JOIN takes JOIN course
GROUP BY student.ID