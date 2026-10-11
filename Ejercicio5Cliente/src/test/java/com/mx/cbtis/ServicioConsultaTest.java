package com.mx.cbtis;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio de consulta del frontend (GestorController) con la API REST simulada:
 * no necesita la API ni la base de datos levantadas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ServicioConsultaTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	RestTemplate restTemplate;

	MockRestServiceServer api;

	@BeforeEach
	void simularApi() {
		api = MockRestServiceServer.bindTo(restTemplate).build();
	}

	@Test
	@DisplayName("AUT-E01 El backend devuelve proyectos válidos y el servicio los entrega a la vista")
	void consultaExitosa() throws Exception {
		api.expect(requestTo("http://localhost:8081/buscarProyectos")).andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess("[{\"id_proyecto\":197,\"nombre\":\"My API Rest\",\"estado_proyecto\":\"En proceso\"}]",
					MediaType.APPLICATION_JSON));

		mvc.perform(post("/consultarProyectos").header("X-Requested-With", "XMLHttpRequest"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id_proyecto").value(197))
			.andExpect(jsonPath("$[0].nombre").value("My API Rest"));
		api.verify();
	}

	@Test
	@DisplayName("AUT-F01 El backend devuelve un error o no responde y el servicio lo controla con un mensaje")
	void errorDelBackendControlado() throws Exception {
		// 1) La API responde 500
		api.expect(requestTo("http://localhost:8081/buscarProyectos")).andRespond(withServerError());
		mvc.perform(post("/consultarProyectos").header("X-Requested-With", "XMLHttpRequest"))
			.andExpect(status().isBadGateway())
			.andExpect(jsonPath("$.error").value("El servidor de datos respondió con un error. Intente más tarde."));
		api.verify();

		// 2) La API no está disponible (conexión rechazada)
		api.reset();
		api.expect(requestTo("http://localhost:8081/getUsuarioLogin"))
			.andRespond(request -> { throw new ResourceAccessException("Connection refused"); });
		mvc.perform(post("/verificarLogin").header("X-Requested-With", "XMLHttpRequest")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"perla22522@cbtis75.edu.mx\",\"contrasena\":\"12345\"}"))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.error").value("No se pudo conectar con el servidor de datos. Intente más tarde."));
		api.verify();
	}
}
