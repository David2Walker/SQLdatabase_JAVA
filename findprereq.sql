SELECT c1.title course, case when prereq_id is null then ' ' when prereq_id is not null
			then (select c2.title from course c2 where c2.course_id = prereq_id) end prereq
			from course c1 LEFT OUTER JOIN prereq 
			USING ( course_id );