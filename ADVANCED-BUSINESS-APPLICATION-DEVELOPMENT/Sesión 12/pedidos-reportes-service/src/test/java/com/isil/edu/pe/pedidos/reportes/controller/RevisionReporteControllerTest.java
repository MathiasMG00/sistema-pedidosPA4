package com.isil.edu.pe.pedidos.reportes.controller;

import com.isil.edu.pe.pedidos.reportes.service.RevisionReporteService;
import com.isil.edu.pe.pedidos.reportes.service.RevisionReporteService.Estado;
import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenRevision;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RevisionReporteControllerTest {

    private RevisionReporteService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(RevisionReporteService.class);

        mvc = MockMvcBuilders.standaloneSetup(
            new RevisionReporteController(service)
        ).build();
    }

    @Test
    void debeRetornarPedidoRevisadoConTrazabilidad() throws Exception {
        // Arrange
        var pedido = new PedidoRevision(
            1L,
            "Ana Torres",
            "Laptop",
            new BigDecimal("200.00"),
            true,
            "Pedido verificado",
            LocalDateTime.of(2026, 11, 23, 10, 30)
        );

        when(service.listar(Estado.REVISADO))
            .thenReturn(List.of(pedido));

        // Act y Assert
        mvc.perform(get("/api/v1/pedidos/revisiones")
                .param("estado", "REVISADO"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
            .andExpect(jsonPath("$[0].productoNombre").value("Laptop"))
            .andExpect(jsonPath("$[0].total").value(200.00))
            .andExpect(jsonPath("$[0].revisado").value(true))
            .andExpect(jsonPath("$[0].observacion")
                .value("Pedido verificado"))
            .andExpect(jsonPath("$[0].fechaRevision").exists());

        verify(service).listar(Estado.REVISADO);
    }

    @Test
    void debeRetornarResumenEnJson() throws Exception {
        // Arrange
        when(service.obtenerResumenRevision())
            .thenReturn(new ResumenRevision(
                4L,
                3L,
                1L,
                new BigDecimal("75.00")
            ));

        // Act y Assert
        mvc.perform(get(
                "/api/v1/pedidos/estadisticas/revisiones"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalPedidos").value(4))
            .andExpect(jsonPath("$.totalRevisados").value(3))
            .andExpect(jsonPath("$.totalPendientes").value(1))
            .andExpect(jsonPath("$.porcentajeRevisado").value(75.00));

        verify(service).obtenerResumenRevision();
    }

    @Test
    void debeResponder400ParaEstadoInvalido() throws Exception {
        // Act y Assert
        mvc.perform(get("/api/v1/pedidos/revisiones")
                .param("estado", "INVALIDO"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}