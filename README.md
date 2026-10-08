# admin-proyectos
Este es un sistema de administrador de proyectos en lenguaje java y Spring boot conectado a base de datos relacional

## Cómo levantar el proyecto (PostgreSQL)

Requisitos: JDK 17 o superior y PostgreSQL (usuario `postgres` / contraseña `postgres`).
No hace falta instalar Maven, se usa `mvnw`.

1. Crear y cargar la base de datos:
   ```
   psql -U postgres -h localhost -c "CREATE DATABASE dbgestor;"
   psql -U postgres -h localhost -d dbgestor -f sql/postgres/01_schema.sql
   psql -U postgres -h localhost -d dbgestor -f sql/postgres/02_data.sql
   ```
   Otras credenciales: variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.
2. API REST (puerto 8081): `cd Ejercicio5MyAPIGestor` y `mvnw spring-boot:run`
3. Cliente web (puerto 8080): `cd Ejercicio5Cliente` y `mvnw spring-boot:run`
   (si el 8080 está ocupado: `mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082`)

## Pruebas

- Automáticas + cobertura: `cd Ejercicio5MyAPIGestor` y `mvnw test` → reporte en `target/site/jacoco/index.html`
- Manuales de base de datos: `psql -U postgres -h localhost -d dbgestor -f sql/postgres/03_pruebas_manuales.sql`
- Endpoints (Práctica 1): importar `docs/postman/Gestor_Practica1.postman_collection.json` en Postman; consultas de BD en `sql/postgres/04_pruebas_postman.sql`
- Reproducir las fallas corregidas (P-36 a P-38): `mvnw spring-boot:run "-Dspring-boot.run.arguments=--gestor.validaciones.activas=false"`
