package com.isil.edu.pe.pedidos.reportes.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevision;

public record PedidoRevisionDto(
    long id,
    String cliente,
    String productoNombre,
    BigDecimal total,
    boolean revisado,
    String observacion,
    LocalDateTime fechaRevision
) {
    public static PedidoRevisionDto from(PedidoRevision resumen) {
        return new PedidoRevisionDto(
            resumen.id(),
            resumen.cliente(),
            resumen.productoNombre(),
            resumen.total(),
            resumen.revisado(),
            resumen.observacion(),
            resumen.fechaRevision()
        );
    }
}
