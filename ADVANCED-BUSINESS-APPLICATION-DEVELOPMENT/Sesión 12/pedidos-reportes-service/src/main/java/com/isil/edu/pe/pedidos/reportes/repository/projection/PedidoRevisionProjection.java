package com.isil.edu.pe.pedidos.reportes.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRevisionProjection(
    long id,
    String cliente,
    String productoNombre,
    BigDecimal total,
    boolean revisado,
    String observacion,
    LocalDateTime fechaRevision
) {
}
