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

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

@WebMvcTest(AsignacionesController.class)
@Import(VerificadorExistencia.class)
class AsignacionesControllerTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	AsignacionesRepository asignacionestable;

	private final Asignaciones asignacion = new Asignaciones(20, 131, 199, "2024-06-20");

	@Test
	@DisplayName("JU-15 Buscar asignación por id existente e inexistente")
	void buscarAsignacion() throws Exception {
		when(asignacionestable.findById(20)).thenReturn(Optional.of(asignacion));
		when(asignacionestable.findById(1)).thenReturn(Optional.empty());

		mvc.perform(get("/buscarAsignacion").param("id", "20"))
			.andExpect(jsonPath("$.id_usuario").value(131))
			.andExpect(jsonPath("$.id_tarea").value(199));
		mvc.perform(get("/buscarAsignacion").param("id", "1"))
			.andExpect(jsonPath("$.id_asignacion").doesNotExist());
	}

	@Test
	@DisplayName("JU-16 Alta, actualización, baja y listado de asignaciones")
	void crudAsignaciones() throws Exception {
		when(asignacionestable.existsById(anyInt())).thenReturn(true);
		when(asignacionestable.findAll()).thenReturn(Collections.singletonList(asignacion));
		String json = "{\"id_usuario\":131,\"id_tarea\":199,\"fecha_asignacion\":\"2026-09-30\"}";

		mvc.perform(post("/addAsignacion").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(content().string("1"));
		mvc.perform(post("/updateAsignacion").contentType(MediaType.APPLICATION_JSON).content(json.replaceFirst("[{]", "{\"id_asignacion\":20,")))
			.andExpect(content().string("1"));
		mvc.perform(delete("/deleteAsignacion").contentType(MediaType.APPLICATION_JSON).content("{\"id_asignacion\":20}"))
			.andExpect(content().string("1"));
		mvc.perform(post("/buscarAsignaciones"))
			.andExpect(jsonPath("$[0].fecha_asignacion").value("2024-06-20"));

		verify(asignacionestable, org.mockito.Mockito.times(2)).save(any(Asignaciones.class));
		verify(asignacionestable).delete(any(Asignaciones.class));
	}
}
