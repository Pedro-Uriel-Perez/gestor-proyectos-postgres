package com.mx.cbtis;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.microsoft.playwright.Locator;

/**
 * Pruebas en navegador real: frontend (puerto aleatorio) → API REST (localhost:8081) → PostgreSQL.
 * Requieren la API levantada; si no lo está, se omiten.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NavegadorConApiTest extends NavegadorBase {

	static final String API = "http://localhost:8081";
	static final String CORREO_PRUEBA = "aut.playwright@cbtis75.edu.mx";
	static final RestTemplate http = new RestTemplate();

	@BeforeAll
	static void apiDisponible() {
		boolean arriba;
		try {
			http.postForObject(API + "/buscarProyectos", null, String.class);
			arriba = true;
		} catch (Exception e) {
			arriba = false;
		}
		Assumptions.assumeTrue(arriba, "La API REST debe estar levantada en " + API);
	}

	/** Borra por la API los usuarios que hayan creado las pruebas de formulario. */
	@AfterEach
	@SuppressWarnings("unchecked")
	void limpiarUsuariosDePrueba() {
		List<Map<String, Object>> usuarios = http.postForObject(API + "/buscarUsuarios", null, List.class);
		for (Map<String, Object> u : usuarios) {
			if (CORREO_PRUEBA.equals(u.get("correo_electronico"))) {
				http.exchange(API + "/deleteUsuario", HttpMethod.DELETE,
						new HttpEntity<>(Map.of("id_usuario", u.get("id_usuario"))), Integer.class);
			}
		}
	}

	@SuppressWarnings("unchecked")
	boolean usuarioGuardadoEnBD() {
		List<Map<String, Object>> usuarios = http.postForObject(API + "/buscarUsuarios", null, List.class);
		return usuarios.stream().anyMatch(u -> CORREO_PRUEBA.equals(u.get("correo_electronico")));
	}

	void llenarRegistro(boolean conRol) {
		page.navigate(url("/register"));
		page.fill("input[name=nombre]", "Usuario Playwright");
		page.fill("input[name=correo_electronico]", CORREO_PRUEBA);
		page.fill("input[name=contrasena]", "Prueba123");
		if (conRol) {
			page.selectOption("select[name=rol]", "Estudiante");
		}
	}

	@Test
	@DisplayName("AUT-E02 Formulario de registro con datos válidos: se envía y el usuario se guarda")
	void registroValido() {
		llenarRegistro(true);
		page.click("button[type=submit]");
		page.waitForURL(Pattern.compile(".*/login$"));
		captura("AUT-E02");

		assertThat(page).hasURL(Pattern.compile(".*/login$"));
		org.junit.jupiter.api.Assertions.assertTrue(usuarioGuardadoEnBD(), "El usuario debe quedar guardado en la base de datos");
	}

	@Test
	@DisplayName("AUT-F02 Formulario de registro sin rol: la validación impide el envío y muestra el error")
	void registroSinRol() {
		llenarRegistro(false);
		page.click("button[type=submit]");
		page.waitForTimeout(1500);
		captura("AUT-F02");

		assertThat(page).hasURL(Pattern.compile(".*/register$"));
		assertThat(page.locator("#mensajeRol")).isVisible();
		org.junit.jupiter.api.Assertions.assertFalse(usuarioGuardadoEnBD(), "No debe guardarse un usuario sin rol");
	}

	@Test
	@DisplayName("AUT-E03 Vista de inicio: después del login se muestran los proyectos de la base de datos")
	void vistaInicioMuestraProyectos() {
		page.navigate(url("/login"));
		page.fill("#exampleInputEmail1", "perla22522@cbtis75.edu.mx");
		page.fill("#exampleInputPassword1", "12345");
		page.click("form button.btn-primary");
		page.waitForURL(Pattern.compile(".*/inicio$"));
		Locator tarjeta = page.locator("#cardContainer .card-title", new com.microsoft.playwright.Page.LocatorOptions().setHasText("My API Rest"));
		tarjeta.waitFor();
		captura("AUT-E03");

		assertThat(tarjeta).isVisible();
	}

	@Test
	@DisplayName("AUT-F03 Vista de proyecto inexistente: la interfaz informa que el proyecto no existe")
	void proyectoInexistente() {
		sesionIniciada();
		page.navigate(url("/proyecto?id_proyecto=999999"));
		page.waitForTimeout(1500);
		captura("AUT-F03");

		assertThat(page.locator("#mensajeProyecto")).isVisible();
		assertThat(page.locator("#mensajeProyecto")).containsText("no existe");
	}
}
