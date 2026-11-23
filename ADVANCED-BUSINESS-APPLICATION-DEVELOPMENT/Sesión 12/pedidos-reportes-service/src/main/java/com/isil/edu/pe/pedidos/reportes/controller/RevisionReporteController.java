package com.isil.edu.pe.pedidos.reportes.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.isil.edu.pe.pedidos.reportes.controller.dto.PedidoRevisionDto;
import com.isil.edu.pe.pedidos.reportes.controller.dto.ResumenRevisionDto;
import com.isil.edu.pe.pedidos.reportes.service.RevisionReporteService;
import com.isil.edu.pe.pedidos.reportes.service.RevisionReporteService.Estado;

@RestController 
@RequestMapping("/api/v1/pedidos")
public class RevisionReporteController {

    private final RevisionReporteService service;

    public RevisionReporteController(RevisionReporteService service) {
        this.service = service;
    }

    @GetMapping(value = "/revisiones", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PedidoRevisionDto> listar(
            @RequestParam(name = "estado") Estado estado) {
        return service.listar(estado).stream()
            .map(PedidoRevisionDto::from)
            .toList();
    }

    @GetMapping(value = "/estadisticas/revisiones", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResumenRevisionDto> estadisticas() {
        return ResponseEntity.ok(ResumenRevisionDto.from(service.obtenerResumenRevision()));
    }

}
