package com.isil.edu.pe.spedidos.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository 
public class PedidoRevisionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public PedidoRevisionJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int marcarRevisado(Long pedidoId){
        String sql = "UPDATE pedido SET revisado = true WHERE id = ? AND revisado = FALSE";
        return jdbcTemplate.update(sql, pedidoId);
    }

    public int insertarRevision(Long pedidoId, String observacion){
        String sql = "INSERT INTO pedido_revision (pedido_id, observacion, fecha_revision) VALUES (?,?, NOW())";
        return jdbcTemplate.update(sql, pedidoId, observacion);
    }

    public boolean existe(Long pedidoId){
        String sql = "SELECT COUNT(*) FROM pedido WHERE id = ?";

        Long existe = jdbcTemplate.queryForObject(sql, Long.class, pedidoId);

        return existe != null && existe > 0;
    }

}
