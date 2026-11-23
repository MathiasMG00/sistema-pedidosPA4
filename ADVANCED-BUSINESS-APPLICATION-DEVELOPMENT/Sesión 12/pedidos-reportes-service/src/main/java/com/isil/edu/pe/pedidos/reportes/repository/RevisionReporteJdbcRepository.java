package com.isil.edu.pe.pedidos.reportes.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.isil.edu.pe.pedidos.reportes.repository.projection.PedidoRevisionProjection;

@Repository
public class RevisionReporteJdbcRepository {


    private final JdbcTemplate jdbcTemplate;

    public RevisionReporteJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PedidoRevisionProjection> listar(boolean revisado) {
        
        String sql = """
            SELECT p.id, p.cliente, p.producto_nombre, p.total,
                   p.revisado, r.observacion, r.fecha_revision
            FROM pedido p
            LEFT JOIN pedido_revision r ON r.pedido_id = p.id
            WHERE p.revisado = ?
            ORDER BY p.id DESC
            """;
        
        return jdbcTemplate.query(sql,
            (rs, fila) -> {
                Timestamp fecha = rs.getTimestamp("fecha_revision");

                return new PedidoRevisionProjection(
                    rs.getLong("id"),
                    rs.getString("cliente"),
                    rs.getString("producto_nombre"),
                    rs.getBigDecimal("total"),
                    rs.getBoolean("revisado"),
                    rs.getString("observacion"),
                    fecha == null ? null : fecha.toLocalDateTime()
                );
            },
            revisado
        );
    }

    public Conteos contar() {

        String sql = """
            SELECT COUNT(*) AS total,
                   COALESCE(
                       SUM(CASE WHEN revisado = TRUE THEN 1 ELSE 0 END),
                       0
                   ) AS revisados
            FROM pedido
            """;

        return jdbcTemplate.queryForObject(sql,
            (rs, fila) -> new Conteos(
                rs.getLong("total"),
                rs.getLong("revisados")
            )
        );
    }

    public record Conteos(long total, long revisados) {
    }
}
