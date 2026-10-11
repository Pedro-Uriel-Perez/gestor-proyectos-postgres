package com.mx.cbtis;

import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Abre un navegador Chromium real (sin ventana) contra el frontend levantado en un puerto aleatorio.
 * Cada prueba guarda una captura en target/evidencias-p2/ID.png como evidencia.
 */
abstract class NavegadorBase {

	static Playwright playwright;
	static Browser browser;

	@LocalServerPort
	int puerto;

	BrowserContext contexto;
	Page page;

	@BeforeAll
	static void abrirNavegador() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
	}

	@AfterAll
	static void cerrarNavegador() {
		browser.close();
		playwright.close();
	}

	@BeforeEach
	void nuevaPestana() {
		contexto = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 800).setLocale("es-MX"));
		page = contexto.newPage();
	}

	@AfterEach
	void cerrarPestana() {
		contexto.close();
	}

	String url(String ruta) {
		return "http://localhost:" + puerto + ruta;
	}

	/** Guarda la pantalla actual como evidencia (target/evidencias-p2/ID.png). */
	void captura(String id) {
		page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target", "evidencias-p2", id + ".png")).setFullPage(true));
	}

	/** Simula una sesión iniciada (el frontend guarda el usuario en localStorage al hacer login). */
	void sesionIniciada() {
		contexto.addInitScript("localStorage.setItem('usuario', JSON.stringify({id_usuario:196, nombre:'Perla Joceline Martínez Meza',"
				+ " correo_electronico:'perla22522@cbtis75.edu.mx', imagen_usuario:'#805611'}));");
	}
}
