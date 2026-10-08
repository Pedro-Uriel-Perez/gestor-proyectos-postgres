package com.mx.cbtis.gestor.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte los errores por datos invalidos en una respuesta 400 (o 404 si el registro no existe)
 * con un mensaje claro, en lugar de un 500 (error interno) sin explicacion.
 * Se desactiva con gestor.validaciones.activas=false (ver VerificadorExistencia).
 */
@RestControllerAdvice
@ConditionalOnProperty(name = "gestor.validaciones.activas", havingValue = "true", matchIfMissing = true)
public class ManejadorErrores {

	/** La base de datos rechazo el dato (fecha con formato invalido, texto demasiado largo, etc.). */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> datoRechazadoPorBD(DataIntegrityViolationException ex, HttpServletRequest req) {
		String causa = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
		// PostgreSQL agrega lineas de detalle ("Position: ..."); solo se deja la primera
		causa = causa.split("\\R")[0].replaceFirst("^ERROR:\\s*", "");
		return respuesta("Dato inválido: la base de datos rechazó la información enviada", causa, req);
	}

	/** El cuerpo de la peticion no es un JSON valido. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
		return respuesta("JSON inválido o mal formado", NestedExceptionUtils.getMostSpecificCause(ex).getMessage().split("\\R")[0], req);
	}

	/** Falta un parametro (?id=...) o no es del tipo correcto (?id=abc). */
	@ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<Map<String, Object>> parametroInvalido(Exception ex, HttpServletRequest req) {
		return respuesta("Parámetro faltante o inválido", ex.getMessage(), req);
	}

	/** update/delete de un registro que no existe. */
	@ExceptionHandler(VerificadorExistencia.RegistroNoEncontradoException.class)
	public ResponseEntity<Map<String, Object>> registroNoEncontrado(RuntimeException ex, HttpServletRequest req) {
		return respuesta(HttpStatus.NOT_FOUND, "Registro no encontrado", ex.getMessage(), req);
	}

	private ResponseEntity<Map<String, Object>> respuesta(String error, String detalle, HttpServletRequest req) {
		return respuesta(HttpStatus.BAD_REQUEST, error, detalle, req);
	}

	private ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String error, String detalle, HttpServletRequest req) {
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("timestamp", LocalDateTime.now().withNano(0).toString());
		cuerpo.put("status", estado.value());
		cuerpo.put("error", error);
		cuerpo.put("detalle", detalle);
		cuerpo.put("ruta", req.getRequestURI());
		return ResponseEntity.status(estado).body(cuerpo);
	}
}
