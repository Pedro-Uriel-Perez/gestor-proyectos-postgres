package com.mx.cbtis.gestor.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * Evita que update/delete con un id inexistente "funcionen": antes, save() con un id que no existe
 * creaba un registro nuevo y delete() respondia 1 sin borrar nada.
 *
 * Con gestor.validaciones.activas=false se desactiva (junto con ManejadorErrores) para poder
 * reproducir el comportamiento anterior al documentar las pruebas.
 */
@Component
public class VerificadorExistencia {

	@Value("${gestor.validaciones.activas:true}")
	private boolean activas;

	public <ID> void exigir(CrudRepository<?, ID> repositorio, ID id, String entidad) {
		if (activas && (id == null || !repositorio.existsById(id))) {
			throw new RegistroNoEncontradoException(entidad, id);
		}
	}

	public static class RegistroNoEncontradoException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public RegistroNoEncontradoException(String entidad, Object id) {
			super(id == null ? "Debe indicar el id del " + entidad : "No existe el " + entidad + " con id " + id);
		}
	}
}
