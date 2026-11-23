package com.isil.edu.pe.spedidos.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table(name = "pedido_revision",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_pedido_revision",
        columnNames = "pedido_id"
    )
)
public class PedidoRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "pedido_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_revision_pedido")
    )
    private Pedido pedido;

    @Column(nullable = false, length = 500)
    private String observacion;

    @Column(name="fecha_revision", nullable = false)
    private LocalDateTime fechaRevision;

    public PedidoRevision(){
    }    
}