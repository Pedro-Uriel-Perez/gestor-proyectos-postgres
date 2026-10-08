-- =====================================================================
-- Datos iniciales de dbgestor (los mismos del dump de MariaDB)
-- Uso:
--   psql -U postgres -h localhost -d dbgestor -f sql/postgres/02_data.sql
-- =====================================================================
SET client_encoding = 'UTF8';

INSERT INTO usuario (id_usuario, nombre, correo_electronico, contrasena, rol, imagen_usuario) VALUES
  (131, 'Ana Paola Ramírez Vázquez',    'ana22533@cbtis75.edu.mx',     '0722',  'Estudiante', '#70FAB7'),
  (172, 'Liliana Gómez Martínez',       'liliana22512@cbtis75.edu.mx', 'ada',   'Estudiante', '#54EA01'),
  (196, 'Perla Joceline Martínez Meza', 'perla22522@cbtis75.edu.mx',   '12345', 'Estudiante', '#805611');

INSERT INTO proyectos (id_proyecto, nombre, descripcion, fecha_inicio, fecha_finalizacion, estado_proyecto, fondo) VALUES
  (197, 'My API Rest', 'Desarrollaran un Api Rest, que satisfaga un sistema de información con al menos 5 tablas.',
   '2024-05-31', '2024-06-29', 'En proceso', '/Images/animacion1.gif');

INSERT INTO tareas (id_tarea, nombre, descripcion, fecha_vencimiento, estado, lista, id_proyecto) VALUES
  (199, 'Base de datos', 'Realizar la estructura de las tablas', '2024-06-13', 'danger', 'terminado', 197);

INSERT INTO comentarios (id_comentario, texto_comentario, fecha_hora, id_usuario, id_tarea) VALUES
  (201, 'Subirlo YA!!!', '2024-06-26 13:48:00', 'Perla Joceline Martínez Meza', 199),
  (202, 'hola',          '2024-06-26 13:49:00', 'Liliana Gómez Martínez',       199);

-- El siguiente id que entregue Hibernate sera 203 (en MariaDB: hibernate_sequence.next_val = 203)
SELECT setval('hibernate_sequence', 203, false);
