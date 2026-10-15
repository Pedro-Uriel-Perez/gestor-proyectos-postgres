package com.mx.cbtis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.modelo.Asignaciones;
import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

/**
 * Los modelos del frontend viajan como JSON entre la vista, el frontend y la API.
 * Se comprueba que cada campo se lee (JSON → objeto) y se escribe (objeto → JSON) con el mismo nombre que usa la API.
 */
class ModeloJsonTest {

	final ObjectMapper json = new ObjectMapper();

	@Test
	@DisplayName("COB-11 Usuario se convierte desde y hacia el JSON de la API")
	void usuario() throws Exception {
		Usuario u = json.readValue("{\"id_usuario\":196,\"nombre\":\"Perla\",\"correo_electronico\":\"perla22522@cbtis75.edu.mx\","
				+ "\"contrasena\":\"12345\",\"rol\":\"Estudiante\",\"imagen_usuario\":\"#805611\"}", Usuario.class);
		assertThat(u.getId_usuario()).isEqualTo(196);
		assertThat(u.getNombre()).isEqualTo("Perla");
		assertThat(u.getCorreo_electronico()).isEqualTo("perla22522@cbtis75.edu.mx");
		assertThat(u.getContrasena()).isEqualTo("12345");
		assertThat(u.getRol()).isEqualTo("Estudiante");
		assertThat(u.getImagen_usuario()).isEqualTo("#805611");

		JsonNode salida = json.valueToTree(new Usuario(1, "Ana", "a@x.mx", "1", "Docente", "#000000"));
		assertThat(salida.get("correo_electronico").asText()).isEqualTo("a@x.mx");
		assertThat(salida.get("rol").asText()).isEqualTo("Docente");
	}

	@Test
	@DisplayName("COB-12 Proyectos se convierte desde y hacia el JSON de la API")
	void proyectos() throws Exception {
		Proyectos p = json.readValue("{\"id_proyecto\":197,\"nombre\":\"My API Rest\",\"descripcion\":\"desc\",\"fecha_inicio\":\"2024-05-31\","
				+ "\"fecha_finalizacion\":\"2024-06-29\",\"estado_proyecto\":\"En proceso\",\"fondo\":\"/Images/a.gif\"}", Proyectos.class);
		assertThat(p.getId_proyecto()).isEqualTo(197);
		assertThat(p.getNombre()).isEqualTo("My API Rest");
		assertThat(p.getDescripcion()).isEqualTo("desc");
		assertThat(p.getFecha_inicio()).isEqualTo("2024-05-31");
		assertThat(p.getFecha_finalizacion()).isEqualTo("2024-06-29");
		assertThat(p.getEstado_proyecto()).isEqualTo("En proceso");
		assertThat(p.getFondo()).isEqualTo("/Images/a.gif");

		JsonNode salida = json.valueToTree(new Proyectos(2, "Nuevo", "d", "2026-01-01", "2026-02-01", "Finalizado", "/Images/b.png"));
		assertThat(salida.get("estado_proyecto").asText()).isEqualTo("Finalizado");
		assertThat(salida.get("fecha_finalizacion").asText()).isEqualTo("2026-02-01");
	}

	@Test
	@DisplayName("COB-13 Tareas se convierte desde y hacia el JSON de la API")
	void tareas() throws Exception {
		Tareas t = json.readValue("{\"id_tarea\":199,\"nombre\":\"Base de datos\",\"descripcion\":\"desc\",\"fecha_vencimiento\":\"2024-06-13\","
				+ "\"lista\":\"terminado\",\"estado\":\"danger\",\"id_proyecto\":197}", Tareas.class);
		assertThat(t.getId_tarea()).isEqualTo(199);
		assertThat(t.getNombre()).isEqualTo("Base de datos");
		assertThat(t.getDescripcion()).isEqualTo("desc");
		assertThat(t.getFecha_vencimiento()).isEqualTo("2024-06-13");
		assertThat(t.getLista()).isEqualTo("terminado");
		assertThat(t.getEstado()).isEqualTo("danger");
		assertThat(t.getId_proyecto()).isEqualTo(197);

		JsonNode salida = json.valueToTree(new Tareas(3, "Nueva", "d", "2026-01-01", "pendiente", "primary", 197));
		assertThat(salida.get("lista").asText()).isEqualTo("pendiente");
		assertThat(salida.get("id_proyecto").asInt()).isEqualTo(197);
	}

	@Test
	@DisplayName("COB-14 Comentarios se convierte desde y hacia el JSON de la API")
	void comentarios() throws Exception {
		Comentarios c = json.readValue("{\"id_comentario\":201,\"id_usuario\":\"Perla\",\"id_tarea\":199,"
				+ "\"texto_comentario\":\"hola\",\"fecha_hora\":\"2024-06-26 13:48:00\"}", Comentarios.class);
		assertThat(c.getId_comentario()).isEqualTo(201);
		assertThat(c.getId_usuario()).isEqualTo("Perla");
		assertThat(c.getId_tarea()).isEqualTo(199);
		assertThat(c.getTexto_comentario()).isEqualTo("hola");
		assertThat(c.getFecha_hora()).isEqualTo("2024-06-26 13:48:00");

		JsonNode salida = json.valueToTree(new Comentarios(4, "Ana", 199, "texto", "2026-10-15 10:00"));
		assertThat(salida.get("texto_comentario").asText()).isEqualTo("texto");
		assertThat(salida.get("fecha_hora").asText()).isEqualTo("2026-10-15 10:00");
	}

	@Test
	@DisplayName("COB-15 Asignaciones se convierte desde y hacia el JSON de la API")
	void asignaciones() throws Exception {
		Asignaciones a = json.readValue("{\"id_asignacion\":20,\"id_usuario\":131,\"id_tarea\":199,\"fecha_asignacion\":\"2024-06-20\"}",
				Asignaciones.class);
		assertThat(a.getId_asignacion()).isEqualTo(20);
		assertThat(a.getId_usuario()).isEqualTo(131);
		assertThat(a.getId_tarea()).isEqualTo(199);
		assertThat(a.getFecha_asignacion()).isEqualTo("2024-06-20");

		JsonNode salida = json.valueToTree(new Asignaciones(5, 196, 199, "2026-10-15"));
		assertThat(salida.get("fecha_asignacion").asText()).isEqualTo("2026-10-15");
	}
}
