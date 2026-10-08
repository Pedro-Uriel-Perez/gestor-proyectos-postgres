package com.mx.cbtis.gestor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integración contra la base PostgreSQL real (dbgestor).
 * Requieren el servicio de PostgreSQL arriba y la base cargada con sql/postgres/01 y 02.
 * Cada prueba corre en una transacción que se revierte al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConexionPostgresTest {

	@Autowired
	DataSource dataSource;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	MockMvc mvc;

	@Autowired
	EntityManager em;

	/** Manda los INSERT pendientes a PostgreSQL y olvida la cache, para leer lo que realmente quedo en la base. */
	private void enviarABase() {
		em.flush();
		em.clear();
	}

	@Test
	@DisplayName("JU-26 La aplicación se conecta a PostgreSQL / dbgestor")
	void conexion() throws Exception {
		try (Connection con = dataSource.getConnection()) {
			assertThat(con.isValid(2)).isTrue();
			assertThat(con.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
			assertThat(con.getCatalog()).isEqualTo("dbgestor");
		}
		assertThat(jdbc.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
	}

	@Test
	@DisplayName("JU-27 Existen las 5 tablas y los datos migrados")
	void tablasYDatos() {
		List<String> tablas = jdbc.queryForList(
				"SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name",
				String.class);
		assertThat(tablas).contains("asignaciones", "comentarios", "proyectos", "tareas", "usuario");
		assertThat(jdbc.queryForObject("SELECT nombre FROM usuario WHERE id_usuario = 131", String.class))
			.isEqualTo("Ana Paola Ramírez Vázquez");
	}

	@Test
	@DisplayName("JU-28 Endpoint de login consulta PostgreSQL")
	void loginContraPostgres() throws Exception {
		mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"perla22522@cbtis75.edu.mx\",\"contrasena\":\"12345\"}"))
			.andExpect(jsonPath("$.id_usuario").value(196));
	}

	@Test
	@DisplayName("JU-29 Alta de proyecto por endpoint se guarda en PostgreSQL con fecha tipo date")
	void altaProyectoPersiste() throws Exception {
		mvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"IT Proyecto\",\"descripcion\":\"prueba\",\"fecha_inicio\":\"2026-09-30\","
						+ "\"fecha_finalizacion\":\"2026-10-30\",\"estado_proyecto\":\"En proceso\",\"fondo\":\"/Images/blanco.png\"}"))
			.andExpect(content().string("1"));
		enviarABase();

		assertThat(jdbc.queryForObject(
				"SELECT fecha_inicio::text FROM proyectos WHERE nombre = 'IT Proyecto'", String.class))
			.isEqualTo("2026-09-30");
	}

	@Test
	@DisplayName("JU-30 Comentario con el formato de fecha del cliente se guarda como timestamp")
	void altaComentarioPersiste() throws Exception {
		mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON)
				.content("{\"texto_comentario\":\"IT comentario\",\"fecha_hora\":\"2026-09-30 19:55\","
						+ "\"id_usuario\":\"Liliana Gómez Martínez\",\"id_tarea\":199}"))
			.andExpect(content().string("1"));
		enviarABase();

		mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":199}"))
			.andExpect(jsonPath("$[?(@.texto_comentario == 'IT comentario')].fecha_hora").value("2026-09-30 19:55:00"));
	}

	@Test
	@DisplayName("JU-31 Buscar proyecto existente por endpoint")
	void buscarProyecto() throws Exception {
		mvc.perform(get("/buscarProyecto").param("id_proyecto", "197"))
			.andExpect(jsonPath("$.nombre").value("My API Rest"));
	}

	@Test
	@DisplayName("JU-32 Update con id inexistente responde 404 y no crea un registro nuevo")
	void updateIdInexistente() throws Exception {
		Integer antes = jdbc.queryForObject("SELECT count(*) FROM usuario", Integer.class);

		mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_usuario\":999999,\"nombre\":\"No existe\",\"correo_electronico\":\"no.existe@prueba.mx\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detalle").value("No existe el usuario con id 999999"));
		enviarABase();

		assertThat(jdbc.queryForObject("SELECT count(*) FROM usuario", Integer.class)).isEqualTo(antes);
	}
}
