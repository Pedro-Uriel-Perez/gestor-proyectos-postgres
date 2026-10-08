package com.mx.cbtis.gestor.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;

/**
 * Pruebas de las consultas nativas de los repositorios sobre una base H2 en memoria
 * (no tocan la base PostgreSQL real).
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class RepositoriosConsultasTest {

	@Autowired
	UsuarioRepository usuarios;

	@Autowired
	ProtectosRepository proyectos;

	@Autowired
	TareasRepository tareas;

	@Autowired
	ComentariosRepository comentarios;

	private Integer idProyecto;
	private Integer idTarea;

	@BeforeEach
	void datos() {
		usuarios.save(new Usuario(null, "Perla", "perla22522@cbtis75.edu.mx", "12345", "Estudiante", "#805611"));
		idProyecto = proyectos.save(new Proyectos(null, "My API Rest", "desc", "2024-05-31", "2024-06-29", "En proceso", "/Images/a.gif")).getId_proyecto();
		idTarea = tareas.save(new Tareas(null, "Base de datos", "desc", "2024-06-13", "terminado", "danger", idProyecto)).getId_tarea();
		comentarios.save(new Comentarios(null, "Perla", idTarea, "Subirlo YA!!!", "2024-06-26 13:48:00"));
		comentarios.save(new Comentarios(null, "Liliana", idTarea, "hola", "2024-06-26 13:49:00"));
	}

	@Test
	@DisplayName("JU-17 findByCorreoPass encuentra al usuario con credenciales válidas")
	void loginValido() {
		assertThat(usuarios.findByCorreoPass("perla22522@cbtis75.edu.mx", "12345"))
			.isPresent()
			.get().extracting(Usuario::getNombre).isEqualTo("Perla");
	}

	@Test
	@DisplayName("JU-18 findByCorreoPass no encuentra con contraseña incorrecta")
	void loginInvalido() {
		assertThat(usuarios.findByCorreoPass("perla22522@cbtis75.edu.mx", "mala")).isEmpty();
	}

	@Test
	@DisplayName("JU-19 findByIdProyecto devuelve solo las tareas del proyecto")
	void tareasPorProyecto() {
		tareas.save(new Tareas(null, "Otra", "de otro proyecto", "2024-06-13", "x", "y", idProyecto + 100));

		List<Tareas> lista = tareas.findByIdProyecto(idProyecto);
		assertThat(lista).hasSize(1);
		assertThat(lista.get(0).getNombre()).isEqualTo("Base de datos");
	}

	@Test
	@DisplayName("JU-20 findByTarea devuelve los comentarios de la tarea")
	void comentariosPorTarea() {
		assertThat(comentarios.findByTarea(idTarea))
			.extracting(Comentarios::getTexto_comentario)
			.containsExactlyInAnyOrder("Subirlo YA!!!", "hola");
		assertThat(comentarios.findByTarea(-1)).isEmpty();
	}

	@Test
	@DisplayName("JU-21 save asigna id automático y count refleja los registros")
	void idAutomatico() {
		assertThat(idProyecto).isNotNull();
		assertThat(idTarea).isNotNull().isNotEqualTo(idProyecto);
		assertThat(comentarios.count()).isEqualTo(2);
	}
}
