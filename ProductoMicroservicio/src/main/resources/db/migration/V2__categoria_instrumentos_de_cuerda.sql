-- La categoría "Guitarras" también contenía el bajo y el ukelele: se renombra a "Instrumentos de cuerda".
-- En una base nueva la tabla está vacía en este punto y no cambia nada (DatosIniciales ya carga la categoría nueva).
update productos set categoria = 'Instrumentos de cuerda' where categoria = 'Guitarras';
