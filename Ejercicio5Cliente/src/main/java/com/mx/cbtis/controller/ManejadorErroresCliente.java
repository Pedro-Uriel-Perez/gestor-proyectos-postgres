package com.mx.cbtis.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controla los errores al comunicarse con la API REST. Antes, cualquier falla del backend
 * terminaba en una página de error 500 o en una petición AJAX sin respuesta para el usuario.
 * - Peticiones AJAX: responde JSON con un mensaje que la vista muestra.
 * - Navegación normal (formularios, páginas): muestra la vista errorServicio.
 */
@ControllerAdvice
public class ManejadorErroresCliente {

	static final String SIN_CONEXION = "No se pudo conectar con el servidor de datos. Intente más tarde.";
	static final String ERROR_SERVIDOR = "El servidor de datos respondió con un error. Intente más tarde.";
	static final String SOLICITUD_INVALIDA = "La operación no se pudo realizar con los datos enviados.";

	/** La API no está levantada o no responde. */
	@ExceptionHandler(ResourceAccessException.class)
	public Object sinConexion(ResourceAccessException ex, HttpServletRequest req) {
		return responder(HttpStatus.SERVICE_UNAVAILABLE, SIN_CONEXION, ex.getMessage(), req);
	}

	/** La API respondió 5xx. */
	@ExceptionHandler(HttpServerErrorException.class)
	public Object errorServidor(HttpServerErrorException ex, HttpServletRequest req) {
		return responder(HttpStatus.BAD_GATEWAY, ERROR_SERVIDOR, ex.getStatusText(), req);
	}

	/** La API rechazó la petición (400 dato inválido, 404 registro inexistente). */
	@ExceptionHandler(HttpClientErrorException.class)
	public Object solicitudInvalida(HttpClientErrorException ex, HttpServletRequest req) {
		return responder(ex.getStatusCode(), SOLICITUD_INVALIDA, ex.getResponseBodyAsString(), req);
	}

	private Object responder(HttpStatusCode estado, String mensaje, String detalle, HttpServletRequest req) {
		if (esAjax(req)) {
			Map<String, Object> cuerpo = new LinkedHashMap<>();
			cuerpo.put("status", estado.value());
			cuerpo.put("error", mensaje);
			cuerpo.put("detalle", detalle);
			return ResponseEntity.status(estado).body(cuerpo);
		}
		ModelAndView vista = new ModelAndView("errorServicio");
		vista.addObject("mensaje", mensaje);
		vista.setStatus(estado);
		return vista;
	}

	private boolean esAjax(HttpServletRequest req) {
		String accept = req.getHeader("Accept");
		return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
				|| (accept != null && accept.contains("application/json"));
	}
}
