SELECT name
FROM instructor NATURAL JOIN teaches
GROUP BY name
HAVING COUNT(*) = (SELECT MAX(cnt) FROM ( SELECT COUNT(*) AS cnt FROM instructor NATURAL JOIN teaches GROUP BY ID) tmp )