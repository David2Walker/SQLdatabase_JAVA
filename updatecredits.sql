drop table if EXISTS studentCopy;
CREATE table studentCopy
SELECT * from student;

update studentCopy S set tot_cred = coalesce(( select sum(credits)
from takes natural join course                         
where S.ID= takes.ID and takes.grade <> 'F' and takes.grade is not null), 0);

SELECT *
FROM studentCopy;
