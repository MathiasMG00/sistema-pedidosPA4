package com.isil.edu.pe.pedidos.reportes.controller.dto;

import java.math.BigDecimal;

import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenRevision;

public record ResumenRevisionDto(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {

    public static ResumenRevisionDto from(ResumenRevision resumen) {
        return new ResumenRevisionDto(
            resumen.totalPedidos(),
            resumen.totalRevisados(),
            resumen.totalPendientes(),
            resumen.porcentajeRevisado()
        );
    }
}
