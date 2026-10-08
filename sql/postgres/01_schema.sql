-- =====================================================================
-- Esquema de la base de datos dbgestor para PostgreSQL
-- Migrado desde el dump de MariaDB 11.3 (dump-dbgestor-202406261453.sql)
--
-- Uso:
--   psql -U postgres -h localhost -c "CREATE DATABASE dbgestor;"
--   psql -U postgres -h localhost -d dbgestor -f sql/postgres/01_schema.sql
-- =====================================================================

DROP TABLE IF EXISTS asignaciones, comentarios, tareas, proyectos, usuario;
DROP SEQUENCE IF EXISTS hibernate_sequence;

-- Hibernate 5 (GenerationType.AUTO) toma los ids de esta secuencia global,
-- igual que hacia con la tabla hibernate_sequence en MariaDB.
CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE usuario (
  id_usuario         integer PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
  nombre             varchar(100),
  correo_electronico varchar(100),
  contrasena         varchar(100),
  rol                varchar(100),
  imagen_usuario     varchar(255)
);

CREATE TABLE proyectos (
  id_proyecto        integer PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
  nombre             varchar(100),
  descripcion        varchar(100),
  fecha_inicio       date,
  fecha_finalizacion date,
  estado_proyecto    varchar(100),
  fondo              varchar(100)
);

CREATE TABLE tareas (
  id_tarea          integer PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
  nombre            varchar(100),
  descripcion       varchar(100),
  fecha_vencimiento date,
  estado            varchar(255),
  lista             varchar(255),
  id_proyecto       integer
);

-- id_usuario guarda el NOMBRE del usuario que comenta (asi lo envia el cliente web)
CREATE TABLE comentarios (
  id_comentario    integer PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
  texto_comentario varchar(250),
  fecha_hora       timestamp,
  id_usuario       varchar(100),
  id_tarea         integer
);

CREATE TABLE asignaciones (
  id_asignacion    integer PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
  id_usuario       integer,
  id_tarea         integer,
  fecha_asignacion date
);

-- Mismos indices que el esquema original (alli tampoco habia llaves foraneas)
CREATE INDEX asignaciones_usuario_fk ON asignaciones (id_usuario);
CREATE INDEX asignaciones_tareas_fk  ON asignaciones (id_tarea);
CREATE INDEX tareas_proyecto_idx     ON tareas (id_proyecto);
CREATE INDEX comentarios_tarea_idx   ON comentarios (id_tarea);
