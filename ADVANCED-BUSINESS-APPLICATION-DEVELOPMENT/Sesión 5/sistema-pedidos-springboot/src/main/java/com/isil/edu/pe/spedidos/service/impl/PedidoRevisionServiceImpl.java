package com.isil.edu.pe.spedidos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;

@Service
public class PedidoRevisionServiceImpl implements PedidoRevisionService{

     private final PedidoRevisionJdbcRepository revisionRep;

     public PedidoRevisionServiceImpl(
            PedidoRevisionJdbcRepository revisionRep) {
        this.revisionRep = revisionRep;
    }

    @Override
    @Transactional
    public void revisarPedido(Long pedidoId, String observacion) {
        if (pedidoId <= 0) {
            throw new PedidoException(
                "El ID del pedido debe ser mayor que cero."
            );
        }

        if (observacion == null || observacion.isBlank()) {
            throw new PedidoException(
                "La observación es obligatoria."
            );
        }

        if (!revisionRep.existe(pedidoId)) {
            throw new PedidoException(
                "El pedido solicitado no existe."
            );
        }

        if (revisionRep.marcarRevisado(pedidoId) != 1) {
            throw new PedidoException(
                "El pedido ya fue revisado."
            );
        }

        if (revisionRep.insertarRevision(pedidoId, observacion) != 1) {
            throw new PedidoException(
                "No fue posible registrar la revisión."
            );
        }
    }
}
