package com.mx.cbtis.gestor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

@WebMvcTest(TareasController.class)
@Import(VerificadorExistencia.class)
class TareasControllerTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	TareasRepository tareastable;

	private final Tareas tarea = new Tareas(199, "Base de datos", "Realizar la estructura de las tablas",
			"2024-06-13", "terminado", "danger", 197);

	@Test
	@DisplayName("JU-09 Obtener tareas de un proyecto")
	void tareasPorProyecto() throws Exception {
		when(tareastable.findByIdProyecto(197)).thenReturn(Collections.singletonList(tarea));

		mvc.perform(post("/getTarea").contentType(MediaType.APPLICATION_JSON).content("{\"id_proyecto\":197}"))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].nombre").value("Base de datos"));
	}

	@Test
	@DisplayName("JU-10 Buscar tarea por id existente e inexistente")
	void buscarTarea() throws Exception {
		when(tareastable.findById(199)).thenReturn(Optional.of(tarea));
		when(tareastable.findById(1)).thenReturn(Optional.empty());

		mvc.perform(get("/buscarTarea").param("id_tarea", "199"))
			.andExpect(jsonPath("$.fecha_vencimiento").value("2024-06-13"));
		mvc.perform(get("/buscarTarea").param("id_tarea", "1"))
			.andExpect(jsonPath("$.id_tarea").doesNotExist());
	}

	@Test
	@DisplayName("JU-11 Alta, actualización, baja y listado de tareas")
	void crudTareas() throws Exception {
		when(tareastable.existsById(anyInt())).thenReturn(true);
		when(tareastable.findAll()).thenReturn(Collections.singletonList(tarea));
		String json = "{\"nombre\":\"Nueva\",\"fecha_vencimiento\":\"2026-10-15\",\"id_proyecto\":197}";

		mvc.perform(post("/addTareas").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(content().string("1"));
		mvc.perform(post("/updateTareas").contentType(MediaType.APPLICATION_JSON).content(json.replaceFirst("[{]", "{\"id_tarea\":199,")))
			.andExpect(content().string("1"));
		mvc.perform(delete("/deleteTareas").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":199}"))
			.andExpect(content().string("1"));
		mvc.perform(post("/buscarTareas"))
			.andExpect(jsonPath("$.length()").value(1));

		verify(tareastable, org.mockito.Mockito.times(2)).save(any(Tareas.class));
		verify(tareastable).delete(any(Tareas.class));
	}

	@Test
	@DisplayName("JU-22 Fecha inválida rechazada por la BD devuelve 400 con mensaje (error controlado)")
	void fechaInvalidaErrorControlado() throws Exception {
		when(tareastable.save(any(Tareas.class))).thenThrow(new DataIntegrityViolationException("could not execute statement",
				new SQLException("ERROR: la sintaxis de entrada no es válida para tipo date: «no-es-fecha»\n  Position: 80")));

		mvc.perform(post("/addTareas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"x\",\"fecha_vencimiento\":\"no-es-fecha\",\"id_proyecto\":197}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.detalle").value("la sintaxis de entrada no es válida para tipo date: «no-es-fecha»"))
			.andExpect(jsonPath("$.ruta").value("/addTareas"));
	}

	@Test
	@DisplayName("JU-23 JSON mal formado y parámetro no numérico devuelven 400")
	void entradasMalFormadas() throws Exception {
		mvc.perform(post("/addTareas").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": "))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("JSON inválido o mal formado"));
		mvc.perform(get("/buscarTarea").param("id_tarea", "abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Parámetro faltante o inválido"));
	}
}
