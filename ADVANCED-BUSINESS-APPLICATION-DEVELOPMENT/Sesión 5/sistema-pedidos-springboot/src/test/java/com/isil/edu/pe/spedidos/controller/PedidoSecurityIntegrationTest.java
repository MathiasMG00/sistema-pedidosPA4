package com.isil.edu.pe.spedidos.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenRevisionResponse;



@SpringBootTest
@ActiveProfiles("test")
class PedidoSecurityIntegrationTest {

  private MockMvc mockMvc;

  @MockitoBean
  private PedidoReporteClient reporteClient;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  @DisplayName("Redirect anonymous user to login when accessing orders page")
  void redirectAnonymousUserToLoginWhenAccessingOrdersPage() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/pedidos"));
    // Assert: no se llama inventario porque seguridad intercepta primero.
    response.andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("Reject PedidoController POST when CONSULTA role submits valid CSRF")
  void rejectPedidoControllerPostWhenConsultaRoleSubmitsValidCsrf() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/pedidos")
        .with(user("consulta").roles("CONSULTA"))
        .with(csrf())
        .param("cliente", "Ana")
        .param("productoCodigo", "P001")
        .param("cantidad", "1"));
    // Assert: no se llega al servicio ni a inventario.
    response.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Reject PedidoController POST when OPERADOR role omits CSRF token")
  void rejectPedidoControllerPostWhenOperadorRoleOmitsCsrfToken() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/pedidos")
        .with(user("operador").roles("OPERADOR"))
        .param("cliente", "Ana")
        .param("productoCodigo", "P001")
        .param("cantidad", "1"));
    // Assert: la validación CSRF ocurre antes del negocio.
    response.andExpect(status().isForbidden());
  }

  @Test
@DisplayName("Permitir supervisión al usuario con rol CONSULTA")
void permitirSupervisionParaRolConsulta() throws Exception {
    // Arrange
    var resumen = new ResumenRevisionResponse(
        0L,
        0L,
        0L,
        BigDecimal.ZERO
    );

    when(reporteClient.listarRevisiones("PENDIENTE"))
        .thenReturn(List.of());

    when(reporteClient.obtenerResumenRevisiones())
        .thenReturn(resumen);

    // Act y Assert
    mockMvc.perform(get("/supervision/pedidos")
            .with(user("consulta").roles("CONSULTA")))
        .andExpect(status().isOk())
        .andExpect(view().name("supervision-pedidos"))
        .andExpect(model().attribute("resumen", resumen));

    verify(reporteClient).listarRevisiones("PENDIENTE");
    verify(reporteClient).obtenerResumenRevisiones();
}
}
