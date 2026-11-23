package com.isil.edu.pe.spedidos.controller;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("supervision/pedidos")
public class PedidoSupervisionController {

    private final PedidoRevisionService revisionService;
    private final PedidoReporteClient reporteClient;

    public PedidoSupervisionController(PedidoRevisionService revisionService, PedidoReporteClient reporteClient) {
        this.revisionService = revisionService;
        this.reporteClient = reporteClient;
    }

    @GetMapping
    public String mostrar(
            @RequestParam(name = "estado", defaultValue = "PENDIENTE") String estado,
            Model model,
            HttpServletResponse response) {

        model.addAttribute("pedidos", List.of());

        if (!"PENDIENTE".equals(estado) && !"REVISADO".equals(estado)) {
            response.setStatus(400);
            model.addAttribute("estado", "PENDIENTE");
            model.addAttribute(
                    "error",
                    "El estado debe ser PENDIENTE o REVISADO.");
            return "supervision-pedidos";
        }

        model.addAttribute("estado", estado);

        try {
            model.addAttribute(
                    "pedidos",
                    reporteClient.listarRevisiones(estado));
            model.addAttribute(
                    "resumen",
                    reporteClient.obtenerResumenRevisiones());
        } catch (PedidoReporteNoDisponibleException e) {
            response.setStatus(503);
            model.addAttribute(
                    "error",
                    "No se pueden consultar los reportes en este momento.");
        }

        return "supervision-pedidos";
    }

    @PostMapping("/{id}/revisar")
    public String revisar(
            @PathVariable("id") long id,
            @RequestParam(name = "observacion", defaultValue = "") String observacion,
            RedirectAttributes redirect) {

        try {
            revisionService.revisarPedido(id, observacion);

            redirect.addFlashAttribute(
                    "mensaje",
                    "El pedido fue revisado correctamente.");
            redirect.addAttribute("estado", "REVISADO");

        } catch (PedidoException e) {
            redirect.addFlashAttribute("error", e.getMessage());

        } catch (DataAccessException e) {
            redirect.addFlashAttribute(
                    "error",
                    "No fue posible completar la revisión.");
        }

        return "redirect:/supervision/pedidos";
    }
}
