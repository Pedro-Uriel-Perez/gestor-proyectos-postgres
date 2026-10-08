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

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;

@WebMvcTest(ProyectosController.class)
@Import(VerificadorExistencia.class)
class ProyectosControllerTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	ProtectosRepository proyectostable;

	private final Proyectos proyecto = new Proyectos(197, "My API Rest", "Api Rest con 5 tablas",
			"2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");

	@Test
	@DisplayName("JU-06 Buscar proyecto por id existente e inexistente")
	void buscarProyecto() throws Exception {
		when(proyectostable.findById(197)).thenReturn(Optional.of(proyecto));
		when(proyectostable.findById(1)).thenReturn(Optional.empty());

		mvc.perform(get("/buscarProyecto").param("id_proyecto", "197"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("My API Rest"))
			.andExpect(jsonPath("$.fecha_inicio").value("2024-05-31"));
		mvc.perform(get("/buscarProyecto").param("id_proyecto", "1"))
			.andExpect(jsonPath("$.id_proyecto").doesNotExist());
	}

	@Test
	@DisplayName("JU-07 Alta, actualización y baja de proyecto devuelven 1")
	void altaActualizacionBaja() throws Exception {
		when(proyectostable.existsById(anyInt())).thenReturn(true);
		String json = "{\"nombre\":\"Nuevo\",\"fecha_inicio\":\"2026-09-30\",\"fecha_finalizacion\":\"2026-10-30\"}";

		mvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(content().string("1"));
		mvc.perform(post("/updateProyecto").contentType(MediaType.APPLICATION_JSON).content(json.replaceFirst("[{]", "{\"id_proyecto\":197,")))
			.andExpect(content().string("1"));
		mvc.perform(delete("/deleteProyecto").contentType(MediaType.APPLICATION_JSON).content("{\"id_proyecto\":197}"))
			.andExpect(content().string("1"));

		verify(proyectostable, org.mockito.Mockito.times(2)).save(any(Proyectos.class));
		verify(proyectostable).delete(any(Proyectos.class));
	}

	@Test
	@DisplayName("JU-08 Listar proyectos")
	void listarProyectos() throws Exception {
		when(proyectostable.findAll()).thenReturn(Collections.singletonList(proyecto));

		mvc.perform(post("/buscarProyectos"))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].estado_proyecto").value("En proceso"));
	}
}
