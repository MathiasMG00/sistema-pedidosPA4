package com.isil.edu.pe.spedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;

@SpringBootTest
@ActiveProfiles("test")
class PedidoRevisionServiceIntegrationTest {

    @Autowired
    private PedidoRevisionService service;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void preparar() {
        quitarRestriccionDePrueba();
        limpiarDatos();
    }

    @AfterEach
    void limpiar() {
        quitarRestriccionDePrueba();
        limpiarDatos();
    }

    @Test
    void debeConfirmarPedidoYRevision() {
        // Arrange
        Long id = crearPedido();

        // Act
        service.revisarPedido(id, "Pedido verificado");

        // Assert
        assertTrue(estaRevisado(id));
        assertEquals(1L, contarRevisiones(id));

        assertEquals(
            "Pedido verificado",
            consultarObservacion(id)
        );

        assertNotNull(jdbc.queryForObject(
            """
            SELECT fecha_revision
            FROM pedido_revision
            WHERE pedido_id = ?
            """,
            java.sql.Timestamp.class,
            id
        ));
    }

    @Test
    void debeRevertirUpdateCuandoFallaInsert() {
        // Arrange
        Long id = crearPedido();

        jdbc.execute("""
            ALTER TABLE pedido_revision
            ADD CONSTRAINT ck_fallo_prueba
            CHECK (observacion <> 'FORZAR_FALLO')
            """);

        // Act
        assertThrows(
            DataAccessException.class,
            () -> service.revisarPedido(id, "FORZAR_FALLO")
        );

        // Assert
        assertFalse(estaRevisado(id));
        assertEquals(0L, contarRevisiones(id));
    }

    @Test
    void debeRechazarSegundaRevisionSinAlterarPrimera() {
        // Arrange
        Long id = crearPedido();
        service.revisarPedido(id, "Primera revisión");

        // Act
        PedidoException error = assertThrows(
            PedidoException.class,
            () -> service.revisarPedido(id, "Segunda revisión")
        );

        // Assert
        assertEquals(
            "El pedido ya fue revisado.",
            error.getMessage()
        );
        assertTrue(estaRevisado(id));
        assertEquals(1L, contarRevisiones(id));
        assertEquals(
            "Primera revisión",
            consultarObservacion(id)
        );
    }

    @Test
    void debeRechazarIdInexistenteSinModificarDatos() {
        // Arrange
        Long idExistente = crearPedido();
        Long idInexistente = idExistente + 1;

        assertEquals(
            0L,
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM pedido WHERE id = ?",
                Long.class,
                idInexistente
            ).longValue()
        );

        // Act
        PedidoException error = assertThrows(
            PedidoException.class,
            () -> service.revisarPedido(
                idInexistente,
                "Pedido verificado"
            )
        );

        // Assert
        assertEquals(
            "El pedido solicitado no existe.",
            error.getMessage()
        );
        assertFalse(estaRevisado(idExistente));
        assertEquals(0L, contarTodasLasRevisiones());
    }

    private Long crearPedido() {
        Pedido pedido = new Pedido(
            "Cliente PA4",
            "LAP-001",
            "Laptop",
            new BigDecimal("100.00"),
            2,
            new BigDecimal("200.00")
        );

        return pedidoRepository.saveAndFlush(pedido).getId();
    }

    private boolean estaRevisado(Long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "SELECT revisado FROM pedido WHERE id = ?",
            Boolean.class,
            id
        ));
    }

    private long contarRevisiones(Long id) {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?",
            Long.class,
            id
        );
    }

    private long contarTodasLasRevisiones() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM pedido_revision",
            Long.class
        );
    }

    private String consultarObservacion(Long id) {
        return jdbc.queryForObject(
            "SELECT observacion FROM pedido_revision WHERE pedido_id = ?",
            String.class,
            id
        );
    }

    private void quitarRestriccionDePrueba() {
        jdbc.execute("""
            ALTER TABLE pedido_revision
            DROP CONSTRAINT IF EXISTS ck_fallo_prueba
            """);
    }

    private void limpiarDatos() {
        jdbc.update("DELETE FROM pedido_revision");
        jdbc.update("DELETE FROM pedido");
    }
}