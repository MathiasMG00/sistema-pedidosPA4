package com.isil.edu.pe.spedidos.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenRevisionResponse;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;

@SpringBootTest
@ActiveProfiles("test")
class PedidoSupervisionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PedidoRevisionService revisionService;

    @MockitoBean
    private PedidoReporteClient reporteClient;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void debeMostrarSupervisionConModel() throws Exception {
        // Arrange
        var resumen = new ResumenRevisionResponse(
            2L,
            1L,
            1L,
            new BigDecimal("50.00")
        );

        when(reporteClient.listarRevisiones("PENDIENTE"))
            .thenReturn(List.of());

        when(reporteClient.obtenerResumenRevisiones())
            .thenReturn(resumen);

        // Act y Assert
        mvc.perform(get("/supervision/pedidos")
                .with(user("consulta").roles("CONSULTA")))
            .andExpect(status().isOk())
            .andExpect(view().name("supervision-pedidos"))
            .andExpect(model().attribute("estado", "PENDIENTE"))
            .andExpect(model().attribute("pedidos", List.of()))
            .andExpect(model().attribute("resumen", resumen));

        verify(reporteClient).listarRevisiones("PENDIENTE");
        verify(reporteClient).obtenerResumenRevisiones();
    }

    @Test
    void debeRevisarYRedirigirComoOperador() throws Exception {
        // Act
        mvc.perform(post("/supervision/pedidos/1/revisar")
                .with(user("operador").roles("OPERADOR"))
                .with(csrf())
                .param("observacion", "Pedido verificado"))
            // Assert
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(
                "/supervision/pedidos?estado=REVISADO"))
            .andExpect(flash().attribute(
                "mensaje",
                "El pedido fue revisado correctamente."
            ));

        verify(revisionService)
            .revisarPedido(1L, "Pedido verificado");

        verifyNoInteractions(reporteClient);
    }

    @Test
    void debeRechazarPostSinCsrf() throws Exception {
        // Act y Assert
        mvc.perform(post("/supervision/pedidos/1/revisar")
                .with(user("operador").roles("OPERADOR"))
                .param("observacion", "Pedido verificado"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(revisionService);
    }

    @Test
    void debeImpedirRevisionComoConsulta() throws Exception {
        // Act y Assert
        mvc.perform(post("/supervision/pedidos/1/revisar")
                .with(user("consulta").roles("CONSULTA"))
                .with(csrf())
                .param("observacion", "Pedido verificado"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(revisionService);
    }

    @Test
    void debeResponder503CuandoReportesNoEstaDisponible()
            throws Exception {
        // Arrange
        when(reporteClient.listarRevisiones("PENDIENTE"))
            .thenThrow(new PedidoReporteNoDisponibleException(
                "Detalle técnico que no debe mostrarse",
                null
            ));

        // Act y Assert
        mvc.perform(get("/supervision/pedidos")
                .with(user("operador").roles("OPERADOR")))
            .andExpect(status().isServiceUnavailable())
            .andExpect(view().name("supervision-pedidos"))
            .andExpect(model().attribute(
                "error",
                "No se pueden consultar los reportes en este momento."
            ));

        verifyNoInteractions(revisionService);
    }
}
