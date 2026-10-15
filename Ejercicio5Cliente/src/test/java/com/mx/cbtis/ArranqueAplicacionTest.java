package com.mx.cbtis;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Arranca el frontend con su método main, como se hace al ejecutar mvnw spring-boot:run. */
class ArranqueAplicacionTest {

	@Test
	@DisplayName("COB-16 El frontend arranca con su método main en un puerto libre")
	void arrancaConMain() {
		assertThatCode(() -> Ejercicio5ClienteApplication.main(new String[] { "--server.port=0" }))
			.doesNotThrowAnyException();
	}
}
