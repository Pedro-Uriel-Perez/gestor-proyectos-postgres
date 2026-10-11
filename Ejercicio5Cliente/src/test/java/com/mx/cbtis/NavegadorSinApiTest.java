package com.mx.cbtis;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Pruebas en navegador real con la API REST caída: el frontend apunta a un puerto donde no hay nada.
 * Comprueban que la interfaz controla el error en lugar de quedarse sin respuesta.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "api.url=http://localhost:1")
class NavegadorSinApiTest extends NavegadorBase {

	@Test
	@DisplayName("AUT-F04 Vista de inicio sin conexión con la API: se informa que no se pudieron cargar los proyectos")
	void inicioSinApi() {
		page.navigate(url("/inicio"));
		page.waitForTimeout(1500);
		captura("AUT-F04");

		assertThat(page.locator("#mensajeInicio")).isVisible();
		assertThat(page.locator("#mensajeInicio")).containsText("No se pudieron cargar los proyectos");
	}

	@Test
	@DisplayName("AUT-F05 Login sin conexión con la API: se muestra un mensaje de error de conexión")
	void loginSinApi() {
		page.navigate(url("/login"));
		page.fill("#exampleInputEmail1", "perla22522@cbtis75.edu.mx");
		page.fill("#exampleInputPassword1", "12345");
		page.click("form button.btn-primary");
		page.waitForTimeout(1500);
		captura("AUT-F05");

		assertThat(page.locator("#alertaServidor")).isVisible();
		assertThat(page.locator("#alertaServidor")).containsText("No se pudo conectar con el servidor");
	}
}
