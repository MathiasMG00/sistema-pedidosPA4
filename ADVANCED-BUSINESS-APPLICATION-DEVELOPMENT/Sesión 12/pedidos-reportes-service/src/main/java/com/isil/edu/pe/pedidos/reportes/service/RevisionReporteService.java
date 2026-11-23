package com.isil.edu.pe.pedidos.reportes.service;

import java.util.List;

import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenRevision;


public interface RevisionReporteService {
    enum Estado {
        PENDIENTE,
        REVISADO
    }

    List<PedidoRevision> listar(Estado estado);

    ResumenRevision obtenerResumenRevision();
}
