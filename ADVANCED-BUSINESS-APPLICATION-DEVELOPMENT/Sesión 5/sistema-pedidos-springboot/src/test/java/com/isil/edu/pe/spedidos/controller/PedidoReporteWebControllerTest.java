package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class PedidoReporteWebControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private PedidoReporteClient reporteClient;

  private MockMvc mockMvc;

  @BeforeEach
  void preparar() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void renderReportViewWithExpectedModelForConsultaUser()
      throws Exception {
    // Arrange
    var resumen = new ResumenPedidosResponse(
        0L,
        0L,
        BigDecimal.ZERO,
        BigDecimal.ZERO);

    when(reporteClient.listarProductosMasVendidos())
        .thenReturn(List.of());

    when(reporteClient.buscarPorCliente("Ana"))
        .thenReturn(List.of());

    when(reporteClient.obtenerResumen())
        .thenReturn(resumen);

    // Act y Assert
    mockMvc.perform(get("/reportes/pedidos")
        .param("cliente", "Ana")
        .with(user("consulta").roles("CONSULTA")))
        .andExpect(status().isOk())
        .andExpect(view().name("reportes-pedidos"))
        .andExpect(model().attribute("cliente", "Ana"))
        .andExpect(model().attribute("productos", List.of()))
        .andExpect(model().attribute("pedidos", List.of()))
        .andExpect(model().attribute("resumen", resumen));

    verify(reporteClient).listarProductosMasVendidos();
    verify(reporteClient).buscarPorCliente("Ana");
    verify(reporteClient).obtenerResumen();
  }
}