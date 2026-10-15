package com.mx.cbtis;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.AfterEach;
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
 * Pruebas de cada endpoint del frontend (GestorController) con la API REST simulada:
 * se verifica que llama al endpoint correcto de la API y que entrega la vista o el JSON esperado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GestorControllerTest {

	static final String API = "http://localhost:8081";
	static final String JSON = MediaType.APPLICATION_JSON_VALUE;
	static final String UNO = "1";

	@Autowired
	MockMvc mvc;

	@Autowired
	RestTemplate restTemplate;

	MockRestServiceServer api;

	@BeforeEach
	void simularApi() {
		api = MockRestServiceServer.bindTo(restTemplate).build();
	}

	@AfterEach
	void verificarLlamadas() {
		api.verify();
	}

	// ---------- Páginas ----------

	@Test
	@DisplayName("COB-01 Las páginas públicas se muestran (home, tutorial, manual, login, registro, inicio)")
	void paginas() throws Exception {
		mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
		mvc.perform(get("/tutorial")).andExpect(status().isOk()).andExpect(view().name("tutorial"));
		mvc.perform(get("/manual")).andExpect(status().isOk()).andExpect(view().name("manual"));
		mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
		mvc.perform(get("/register")).andExpect(status().isOk()).andExpect(model().attributeExists("usuario"));
		mvc.perform(get("/inicio")).andExpect(status().isOk()).andExpect(model().attributeExists("proyectos"));
	}

	@Test
	@DisplayName("COB-02 La página del proyecto carga los miembros desde la API")
	void paginaProyecto() throws Exception {
		api.expect(requestTo(API + "/buscarUsuarios")).andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess("[{\"id_usuario\":196,\"nombre\":\"Perla\"}]", MediaType.APPLICATION_JSON));

		mvc.perform(get("/proyecto").param("id_proyecto", "197"))
			.andExpect(status().isOk())
			.andExpect(view().name("proyecto"))
			.andExpect(model().attributeExists("tarea", "listaUsuarios"));
	}

	@Test
	@DisplayName("COB-03 La ruta /tarea muestra la vista del proyecto con un formulario de tarea vacío")
	void paginaTarea() throws Exception {
		mvc.perform(get("/tarea")).andExpect(status().isOk()).andExpect(view().name("proyecto"));
	}

	// ---------- Usuarios ----------

	@Test
	@DisplayName("COB-04 Registro de usuario: envía el formulario a /addUsuario y redirige al login")
	void guardarUsuario() throws Exception {
		api.expect(requestTo(API + "/addUsuario")).andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));

		mvc.perform(post("/saveUsuario").param("nombre", "Nuevo").param("correo_electronico", "n@cbtis75.edu.mx")
				.param("contrasena", "1").param("rol", "Estudiante"))
			.andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("COB-05 Login y lista de usuarios se obtienen de la API")
	void loginYUsuarios() throws Exception {
		api.expect(requestTo(API + "/getUsuarioLogin"))
			.andRespond(withSuccess("{\"id_usuario\":196,\"nombre\":\"Perla\"}", MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/buscarUsuarios"))
			.andRespond(withSuccess("[{\"id_usuario\":196},{\"id_usuario\":131}]", MediaType.APPLICATION_JSON));

		mvc.perform(post("/verificarLogin").contentType(JSON).content("{\"correo_electronico\":\"p@x.mx\",\"contrasena\":\"1\"}"))
			.andExpect(jsonPath("$.nombre").value("Perla"));
		mvc.perform(post("/consultarUsuarios"))
			.andExpect(jsonPath("$.length()").value(2));
	}

	// ---------- Proyectos ----------

	@Test
	@DisplayName("COB-06 Proyecto: guardar, buscar para editar y eliminar llaman a la API correcta")
	void proyectos() throws Exception {
		api.expect(requestTo(API + "/addProyecto")).andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/buscarProyecto?id_proyecto=197")).andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("{\"id_proyecto\":197,\"nombre\":\"My API Rest\"}", MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/deleteProyecto")).andExpect(method(HttpMethod.DELETE))
			.andExpect(content().json("{\"id_proyecto\":197}"))
			.andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));

		mvc.perform(post("/saveProyecto").param("nombre", "Nuevo").param("fecha_inicio", "2026-10-15"))
			.andExpect(redirectedUrl("/inicio"));
		mvc.perform(post("/actualizarProyecto").contentType(JSON).content("{\"id_proyecto\":197}"))
			.andExpect(jsonPath("$.nombre").value("My API Rest"));
		mvc.perform(post("/eliminarProyecto").contentType(JSON).content("{\"id_proyecto\":197}"))
			.andExpect(redirectedUrl("/inicio"));
	}

	// ---------- Tareas ----------

	@Test
	@DisplayName("COB-07 Tareas: guardar, consultar por proyecto, buscar, cambiar y eliminar")
	void tareas() throws Exception {
		api.expect(requestTo(API + "/addTareas")).andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/getTarea"))
			.andRespond(withSuccess("[{\"id_tarea\":199,\"nombre\":\"Base de datos\"}]", MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/buscarTarea?id_tarea=199")).andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("{\"id_tarea\":199,\"nombre\":\"Base de datos\"}", MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/addTareas")).andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/addTareas")).andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/buscarTareas"))
			.andRespond(withSuccess("[{\"id_tarea\":199}]", MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/deleteTareas")).andExpect(method(HttpMethod.DELETE))
			.andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));

		mvc.perform(post("/saveTarea").param("nombre", "Nueva").param("id_proyecto", "197"))
			.andExpect(status().is3xxRedirection());
		mvc.perform(post("/obtenerTareas").contentType(JSON).content("{\"id_proyecto\":197}"))
			.andExpect(jsonPath("$[0].nombre").value("Base de datos"));
		mvc.perform(post("/actualizarTarea").contentType(JSON).content("{\"id_tarea\":199}"))
			.andExpect(jsonPath("$.id_tarea").value(199));
		mvc.perform(post("/verificarTarea").contentType(JSON).content("{\"nombre\":\"x\",\"id_proyecto\":197}"))
			.andExpect(jsonPath("$").value(1));
		mvc.perform(post("/cambiosTareas").contentType(JSON).content("{\"id_tarea\":199,\"lista\":\"terminado\"}"))
			.andExpect(jsonPath("$").value(1));
		mvc.perform(post("/consultarTareas"))
			.andExpect(jsonPath("$.length()").value(1));
		mvc.perform(post("/eliminarTarea").contentType(JSON).content("{\"id_tarea\":199}"))
			.andExpect(redirectedUrl("/proyecto"));
	}

	// ---------- Comentarios ----------

	@Test
	@DisplayName("COB-08 Comentarios: guardar, consultar por tarea y listar")
	void comentarios() throws Exception {
		api.expect(requestTo(API + "/addComentario")).andRespond(withSuccess(UNO, MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/getComentario"))
			.andRespond(withSuccess("[{\"id_comentario\":201,\"texto_comentario\":\"hola\"}]", MediaType.APPLICATION_JSON));
		api.expect(requestTo(API + "/buscarComentarios"))
			.andRespond(withSuccess("[{\"id_comentario\":201},{\"id_comentario\":202}]", MediaType.APPLICATION_JSON));

		mvc.perform(post("/saveComentario").contentType(JSON)
				.content("{\"texto_comentario\":\"hola\",\"fecha_hora\":\"2026-10-15 10:00\",\"id_usuario\":\"Perla\",\"id_tarea\":199}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(1));
		mvc.perform(post("/obtenerComentarios").contentType(JSON).content("{\"id_tarea\":199}"))
			.andExpect(jsonPath("$[0].texto_comentario").value("hola"));
		mvc.perform(post("/consultarComentarios"))
			.andExpect(jsonPath("$.length()").value(2));
	}

	// ---------- Manejo de errores ----------

	@Test
	@DisplayName("COB-09 Error en un formulario sin conexión con la API: se muestra la página de error con mensaje")
	void errorEnFormulario() throws Exception {
		api.expect(requestTo(API + "/addProyecto")).andRespond(request -> { throw new ResourceAccessException("Connection refused"); });

		mvc.perform(post("/saveProyecto").param("nombre", "Nuevo"))
			.andExpect(status().isServiceUnavailable())
			.andExpect(view().name("errorServicio"))
			.andExpect(model().attribute("mensaje", "No se pudo conectar con el servidor de datos. Intente más tarde."));
	}

	@Test
	@DisplayName("COB-17 El tipo de respuesta de error depende de lo que pide el navegador (JSON o página HTML)")
	void tipoDeRespuestaDeError() throws Exception {
		api.expect(requestTo(API + "/buscarProyectos")).andRespond(request -> { throw new ResourceAccessException("Connection refused"); });
		api.expect(requestTo(API + "/buscarProyectos")).andRespond(request -> { throw new ResourceAccessException("Connection refused"); });

		// Pide JSON (Accept: application/json) → mensaje JSON
		mvc.perform(post("/consultarProyectos").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.error").value("No se pudo conectar con el servidor de datos. Intente más tarde."));
		// Pide una página (Accept: text/html) → vista de error
		mvc.perform(post("/consultarProyectos").accept(MediaType.TEXT_HTML))
			.andExpect(status().isServiceUnavailable())
			.andExpect(view().name("errorServicio"));
	}

	@Test
	@DisplayName("COB-10 La API rechaza la operación (404): el frontend responde el mismo código con un mensaje")
	void apiRechazaOperacion() throws Exception {
		api.expect(requestTo(API + "/deleteProyecto")).andRespond(withResourceNotFound());

		mvc.perform(post("/eliminarProyecto").header("X-Requested-With", "XMLHttpRequest")
				.contentType(JSON).content("{\"id_proyecto\":999999}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("La operación no se pudo realizar con los datos enviados."));
	}
}
