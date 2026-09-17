SELECT c.District, COUNT(c.ID)
FROM City AS c
GROUP BY c.District
ORDER BY c.District;