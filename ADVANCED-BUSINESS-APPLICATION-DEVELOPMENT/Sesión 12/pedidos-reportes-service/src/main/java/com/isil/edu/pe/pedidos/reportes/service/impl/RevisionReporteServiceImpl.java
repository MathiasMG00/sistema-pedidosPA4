package com.isil.edu.pe.pedidos.reportes.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.isil.edu.pe.pedidos.reportes.repository.RevisionReporteJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.projection.PedidoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.service.RevisionReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenRevision;

@Service
public class RevisionReporteServiceImpl implements RevisionReporteService {

    private final RevisionReporteJdbcRepository jdbcRepository;

    public RevisionReporteServiceImpl(
            RevisionReporteJdbcRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public List<PedidoRevision> listar(Estado estado) {
        List<PedidoRevisionProjection> pedidos = jdbcRepository.listar(estado == Estado.REVISADO);
        return pedidos.stream()
            .map(p -> new PedidoRevision(
                p.id(),
                p.cliente(),
                p.productoNombre(),
                p.total(),
                p.revisado(),
                p.observacion(),
                p.fechaRevision()
            ))
            .toList();
    }

    @Override
    public ResumenRevision obtenerResumenRevision() {
        var conteos = jdbcRepository.contar();

        BigDecimal porcentaje = conteos.total() == 0
            ? BigDecimal.ZERO.setScale(2)
            : BigDecimal.valueOf(conteos.revisados())
                .multiply(BigDecimal.valueOf(100))
                .divide(
                    BigDecimal.valueOf(conteos.total()),
                    2,
                    RoundingMode.HALF_UP
                );

        return new ResumenRevision(
            conteos.total(),
            conteos.revisados(),
            conteos.total() - conteos.revisados(),
            porcentaje
        );
    }


}
