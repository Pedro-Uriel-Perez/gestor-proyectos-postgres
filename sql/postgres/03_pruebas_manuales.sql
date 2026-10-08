-- =====================================================================
-- Pruebas manuales de conexion y consulta a dbgestor (PostgreSQL)
-- Uso:
--   psql -U postgres -h localhost -d dbgestor -f sql/postgres/03_pruebas_manuales.sql
-- (o copiar cada bloque en pgAdmin > Query Tool)
-- =====================================================================
SET client_encoding = 'UTF8';
\pset footer on

\echo '=== DB-01 Conexion: servidor, base y usuario conectados ==='
SELECT version() AS servidor, current_database() AS base, current_user AS usuario,
       inet_server_port() AS puerto, now() AS fecha_hora_servidor;

\echo '=== DB-02 Estructura: las 5 tablas del sistema existen ==='
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY table_name;

\echo '=== DB-03 Conteo de registros por tabla ==='
SELECT 'usuario' AS tabla, count(*) FROM usuario
UNION ALL SELECT 'proyectos', count(*) FROM proyectos
UNION ALL SELECT 'tareas', count(*) FROM tareas
UNION ALL SELECT 'comentarios', count(*) FROM comentarios
UNION ALL SELECT 'asignaciones', count(*) FROM asignaciones;

\echo '=== DB-04 SELECT de usuarios (datos migrados con acentos) ==='
SELECT id_usuario, nombre, correo_electronico, rol FROM usuario ORDER BY id_usuario;

\echo '=== DB-05 Misma consulta que usa el login de la API ==='
SELECT id_usuario, nombre, rol
FROM usuario
WHERE correo_electronico = 'perla22522@cbtis75.edu.mx' AND contrasena = '12345';

\echo '=== DB-06 JOIN proyecto -> tareas -> comentarios ==='
SELECT p.nombre AS proyecto, t.nombre AS tarea, t.lista, c.texto_comentario, c.id_usuario AS autor, c.fecha_hora
FROM proyectos p
JOIN tareas t      ON t.id_proyecto = p.id_proyecto
LEFT JOIN comentarios c ON c.id_tarea = t.id_tarea
ORDER BY p.id_proyecto, t.id_tarea, c.fecha_hora;

\echo '=== DB-07 Secuencia de ids usada por Hibernate ==='
SELECT last_value, is_called FROM hibernate_sequence;

\echo '=== DB-08 Tipos de dato de las columnas de fecha ==='
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public' AND column_name LIKE 'fecha%'
ORDER BY table_name, column_name;

\echo '=== DB-09 Control negativo: fecha invalida es rechazada (debe dar ERROR) ==='
\set ON_ERROR_STOP 0
INSERT INTO tareas (nombre, fecha_vencimiento) VALUES ('fecha mala', 'no-es-fecha');
\set ON_ERROR_STOP 1

\echo '=== DB-10 Transaccion con ROLLBACK: insertar y deshacer ==='
BEGIN;
INSERT INTO usuario (nombre, correo_electronico, contrasena, rol)
VALUES ('Usuario Temporal', 'temp@prueba.mx', 'x', 'Estudiante');
SELECT id_usuario, nombre FROM usuario WHERE correo_electronico = 'temp@prueba.mx';
ROLLBACK;
SELECT count(*) AS temporales_tras_rollback FROM usuario WHERE correo_electronico = 'temp@prueba.mx';
