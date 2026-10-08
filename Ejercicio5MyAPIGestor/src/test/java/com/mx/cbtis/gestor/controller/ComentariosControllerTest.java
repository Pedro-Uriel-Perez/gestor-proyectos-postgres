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

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@WebMvcTest(ComentariosController.class)
@Import(VerificadorExistencia.class)
class ComentariosControllerTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	ComentariosRepository comentariostable;

	private final Comentarios c1 = new Comentarios(201, "Perla Joceline Martínez Meza", 199, "Subirlo YA!!!", "2024-06-26 13:48:00");
	private final Comentarios c2 = new Comentarios(202, "Liliana Gómez Martínez", 199, "hola", "2024-06-26 13:49:00");

	@Test
	@DisplayName("JU-12 Obtener comentarios de una tarea")
	void comentariosPorTarea() throws Exception {
		when(comentariostable.findByTarea(199)).thenReturn(Arrays.asList(c1, c2));

		mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":199}"))
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[1].texto_comentario").value("hola"));
	}

	@Test
	@DisplayName("JU-13 Buscar comentario por id existente e inexistente")
	void buscarComentario() throws Exception {
		when(comentariostable.findById(201)).thenReturn(Optional.of(c1));
		when(comentariostable.findById(1)).thenReturn(Optional.empty());

		mvc.perform(get("/buscarComentario").param("id", "201"))
			.andExpect(jsonPath("$.id_usuario").value("Perla Joceline Martínez Meza"));
		mvc.perform(get("/buscarComentario").param("id", "1"))
			.andExpect(jsonPath("$.id_comentario").doesNotExist());
	}

	@Test
	@DisplayName("JU-14 Alta, actualización, baja y listado de comentarios")
	void crudComentarios() throws Exception {
		when(comentariostable.existsById(anyInt())).thenReturn(true);
		when(comentariostable.findAll()).thenReturn(Arrays.asList(c1, c2));
		String json = "{\"texto_comentario\":\"nuevo\",\"fecha_hora\":\"2026-09-30 19:55\",\"id_usuario\":\"Perla\",\"id_tarea\":199}";

		mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(content().string("1"));
		mvc.perform(post("/updateComentario").contentType(MediaType.APPLICATION_JSON).content(json.replaceFirst("[{]", "{\"id_comentario\":201,")))
			.andExpect(content().string("1"));
		mvc.perform(delete("/deleteComentario").contentType(MediaType.APPLICATION_JSON).content("{\"id_comentario\":201}"))
			.andExpect(content().string("1"));
		mvc.perform(post("/buscarComentarios"))
			.andExpect(jsonPath("$.length()").value(2));

		verify(comentariostable, org.mockito.Mockito.times(2)).save(any(Comentarios.class));
		verify(comentariostable).delete(any(Comentarios.class));
	}
}
