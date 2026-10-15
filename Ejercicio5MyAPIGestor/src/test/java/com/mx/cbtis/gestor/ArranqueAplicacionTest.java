package com.mx.cbtis.gestor;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Arranca la API con su método main, como se hace al ejecutar mvnw spring-boot:run. */
class ArranqueAplicacionTest {

	@Test
	@DisplayName("JU-33 La API arranca con su método main conectada a PostgreSQL en un puerto libre")
	void arrancaConMain() {
		assertThatCode(() -> Ejercicio5MyApiGestorApplication.main(new String[] { "--server.port=0" }))
			.doesNotThrowAnyException();
	}
}
