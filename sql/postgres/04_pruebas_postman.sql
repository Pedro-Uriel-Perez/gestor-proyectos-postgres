-- =====================================================================
-- Consultas de base de datos de la Práctica 1 (casos P-00, P-25 a P-29, P-35 y P-37)
-- Se ejecutan en psql conectado a dbgestor, copiando cada bloque en el momento indicado.
-- Los registros de prueba son los que crea la colección de Postman
-- (docs/postman/Gestor_Practica1.postman_collection.json).
-- =====================================================================

-- ---------- P-00 Conexión (antes de empezar) ----------
\dt
SELECT current_database() AS base, current_user AS usuario, inet_server_port() AS puerto, version();

-- ---------- P-25 a P-29: después de P-24 y ANTES de las eliminaciones ----------
-- P-25 usuario creado (P-01) y modificado (P-06)
SELECT * FROM usuario WHERE correo_electronico LIKE 'prueba.%@cbtis75.edu.mx';

-- P-26 proyecto creado (P-07) y modificado (P-10)
SELECT * FROM proyectos WHERE nombre LIKE 'Proyecto Prueba Postman%';

-- P-27 tarea creada (P-11) y modificada (P-15)
SELECT * FROM tareas WHERE nombre = 'Tarea Prueba Postman';

-- P-28 comentario creado (P-16) y modificado (P-20)
SELECT * FROM comentarios WHERE texto_comentario LIKE 'Comentario Prueba Postman%';

-- P-29 asignación creada (P-21) y modificada (P-24)
SELECT a.*, u.nombre AS usuario, t.nombre AS tarea
FROM asignaciones a
JOIN usuario u ON u.id_usuario = a.id_usuario
JOIN tareas t  ON t.id_tarea  = a.id_tarea
WHERE u.correo_electronico LIKE 'prueba.%@cbtis75.edu.mx';

-- ---------- P-35: después de P-30 a P-34 (eliminaciones) ----------
-- Todos los conteos deben ser 0
SELECT 'usuario' AS tabla, count(*) FROM usuario WHERE correo_electronico LIKE 'prueba.%@cbtis75.edu.mx'
UNION ALL SELECT 'proyectos', count(*) FROM proyectos WHERE nombre LIKE 'Proyecto Prueba Postman%'
UNION ALL SELECT 'tareas', count(*) FROM tareas WHERE nombre = 'Tarea Prueba Postman'
UNION ALL SELECT 'comentarios', count(*) FROM comentarios WHERE texto_comentario LIKE 'Comentario Prueba Postman%'
UNION ALL SELECT 'asignaciones', count(*) FROM asignaciones WHERE fecha_asignacion = '2026-10-08';

-- ---------- P-37: verificación del update con id inexistente ----------
-- ANTES de la corrección: aparece 1 fila (el update CREÓ un usuario nuevo).
-- DESPUÉS de la corrección: (0 filas).
SELECT * FROM usuario WHERE correo_electronico = 'no.existe@prueba.mx';

-- Limpieza del registro que se creó por la falla (ejecutar después de tomar la captura "antes")
DELETE FROM usuario WHERE correo_electronico = 'no.existe@prueba.mx';
