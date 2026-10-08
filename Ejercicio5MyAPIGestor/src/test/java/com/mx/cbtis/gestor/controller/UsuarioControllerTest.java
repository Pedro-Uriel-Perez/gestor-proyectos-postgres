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

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@WebMvcTest(UsuarioController.class)
@Import(VerificadorExistencia.class)
class UsuarioControllerTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	UsuarioRepository usuariotable;

	private final Usuario perla = new Usuario(196, "Perla Joceline Martínez Meza",
			"perla22522@cbtis75.edu.mx", "12345", "Estudiante", "#805611");

	@Test
	@DisplayName("JU-01 Login con credenciales correctas devuelve el usuario")
	void loginCorrecto() throws Exception {
		when(usuariotable.findByCorreoPass("perla22522@cbtis75.edu.mx", "12345")).thenReturn(Optional.of(perla));

		mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"perla22522@cbtis75.edu.mx\",\"contrasena\":\"12345\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id_usuario").value(196))
			.andExpect(jsonPath("$.rol").value("Estudiante"));
	}

	@Test
	@DisplayName("JU-02 Login con credenciales incorrectas devuelve nombre '000'")
	void loginIncorrecto() throws Exception {
		when(usuariotable.findByCorreoPass(any(), any())).thenReturn(Optional.empty());

		mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"x@x.mx\",\"contrasena\":\"mala\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("000"))
			.andExpect(jsonPath("$.id_usuario").doesNotExist());
	}

	@Test
	@DisplayName("JU-03 Buscar usuario por id existente e inexistente")
	void buscarUsuario() throws Exception {
		when(usuariotable.findById(196)).thenReturn(Optional.of(perla));
		when(usuariotable.findById(999)).thenReturn(Optional.empty());

		mvc.perform(get("/buscarUsuario").param("id", "196"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.correo_electronico").value("perla22522@cbtis75.edu.mx"));
		mvc.perform(get("/buscarUsuario").param("id", "999"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id_usuario").doesNotExist());
	}

	@Test
	@DisplayName("JU-04 Alta, actualización y baja de usuario devuelven 1")
	void altaActualizacionBaja() throws Exception {
		when(usuariotable.existsById(anyInt())).thenReturn(true);
		String json = "{\"nombre\":\"Nuevo\",\"correo_electronico\":\"n@cbtis75.edu.mx\",\"contrasena\":\"1\",\"rol\":\"Estudiante\"}";

		mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(content().string("1"));
		mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON).content(json.replaceFirst("[{]", "{\"id_usuario\":196,")))
			.andExpect(content().string("1"));
		mvc.perform(delete("/deleteUsuario").contentType(MediaType.APPLICATION_JSON).content("{\"id_usuario\":196}"))
			.andExpect(content().string("1"));

		verify(usuariotable, org.mockito.Mockito.times(2)).save(any(Usuario.class));
		verify(usuariotable).delete(any(Usuario.class));
	}

	@Test
	@DisplayName("JU-05 Listar usuarios")
	void listarUsuarios() throws Exception {
		when(usuariotable.findAll()).thenReturn(Arrays.asList(perla, new Usuario()));

		mvc.perform(post("/buscarUsuarios"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
		verify(usuariotable, org.mockito.Mockito.never()).findById(anyInt());
	}

	@Test
	@DisplayName("JU-24 Update y delete con id inexistente devuelven 404 y no tocan la base")
	void idInexistente() throws Exception {
		when(usuariotable.existsById(999)).thenReturn(false);

		mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_usuario\":999,\"nombre\":\"No existe\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("Registro no encontrado"))
			.andExpect(jsonPath("$.detalle").value("No existe el usuario con id 999"));
		mvc.perform(delete("/deleteUsuario").contentType(MediaType.APPLICATION_JSON).content("{\"id_usuario\":999}"))
			.andExpect(status().isNotFound());
		mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Sin id\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detalle").value("Debe indicar el id del usuario"));

		verify(usuariotable, org.mockito.Mockito.never()).save(any(Usuario.class));
		verify(usuariotable, org.mockito.Mockito.never()).delete(any(Usuario.class));
	}
}
