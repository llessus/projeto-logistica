package com.projeto.logistica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistica") // Mudança: base path diferente
public class LogisticaController {

    private final JdbcTemplate jdbcTemplate;

    public LogisticaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Despachar pedido (simulação)
     */
    @PostMapping("/despachar")
    public ResponseEntity<String> despacharPedido(@RequestBody Map<String, Object> dadosDespacho) {
        String role = (String) dadosDespacho.getOrDefault("role", "USER");
        if (!role.equalsIgnoreCase("LOGISTICA") && !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).body("Acesso negado! Somente LOGISTICA ou ADMIN.");
        }

        try {
            Integer idPedido = Integer.parseInt(dadosDespacho.get("idPedido").toString());
            Integer idTransportadora = Integer.parseInt(dadosDespacho.get("idTransportadora").toString());

            // Chama a PROCEDURE do banco
            jdbcTemplate.update("CALL proc_despachar_pedido(?, ?)", idPedido, idTransportadora);
            return ResponseEntity.ok("Pedido despachado! (Procedure chamada, Trigger de Log disparado)");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao despachar: " + e.getMessage());
        }
    }

    /**
     * Consulta status de entregas
     */
    @GetMapping("/status-entregas")
    public ResponseEntity<Object> getStatusEntregas() {
        List<Map<String, Object>> pedidos = jdbcTemplate.queryForList("SELECT * FROM view_detalhes_pedido");
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Consulta logs (simulação)
     */
    @GetMapping("/logs")
    public ResponseEntity<Object> getLogsDeAuditoria(@RequestParam(required = false, defaultValue = "USER") String role) {
        if (!role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).body("Acesso negado! Somente ADMIN.");
        }

        List<Map<String, Object>> logs = jdbcTemplate.queryForList("SELECT * FROM view_auditoria_logs LIMIT 100");
        return ResponseEntity.ok(logs);
    }
}
