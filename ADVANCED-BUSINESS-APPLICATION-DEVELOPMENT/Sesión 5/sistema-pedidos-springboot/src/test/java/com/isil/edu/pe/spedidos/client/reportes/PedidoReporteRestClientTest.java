package com.isil.edu.pe.spedidos.client.reportes;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PedidoReporteRestClientTest {

    private static final String BASE_URL =
        "http://localhost:8083/pedidos-reportes-service/api/v1/pedidos";

    private MockRestServiceServer server;
    private PedidoReporteRestClient client;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        client = new PedidoReporteRestClient(
            builder.build(),
            BASE_URL
        );
    }

    @Test
    void debeTraducirError500DelListado() {
        // Arrange
        server.expect(requestTo(
                BASE_URL + "/revisiones?estado=PENDIENTE"))
            .andRespond(withServerError());

        // Act
        PedidoReporteNoDisponibleException error = assertThrows(
            PedidoReporteNoDisponibleException.class,
            () -> client.listarRevisiones("PENDIENTE")
        );

        // Assert
        assertTrue(error.getMessage().contains("no está disponible"));
        assertNotNull(error.getCause());
        server.verify();
    }

    @Test
    void debeTraducirErrorDeConexionDelResumen() {
        // Arrange
        server.expect(requestTo(
                BASE_URL + "/estadisticas/revisiones"))
            .andRespond(withException(
                new IOException("Conexión interrumpida")
            ));

        // Act
        PedidoReporteNoDisponibleException error = assertThrows(
            PedidoReporteNoDisponibleException.class,
            () -> client.obtenerResumenRevisiones()
        );

        // Assert
        assertTrue(error.getMessage().contains("no está disponible"));
        assertNotNull(error.getCause());
        server.verify();
    }
}
