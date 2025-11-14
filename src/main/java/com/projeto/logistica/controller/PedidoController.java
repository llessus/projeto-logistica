package com.projeto.logistica.controller;

import com.projeto.logistica.model.Pedido;
import com.projeto.logistica.repository.PedidoRepository;
import com.projeto.logistica.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RedisCacheService redisService;

    // 1. LISTAR (READ)
    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    // 2. CRIAR (CREATE) - Novo Método
    @PostMapping
    public Pedido criarPedido(@RequestBody Pedido novoPedido) {
        novoPedido.setDataPedido(LocalDateTime.now());
        novoPedido.setStatus("PENDENTE");
        // O código de rastreio será gerado pelo Banco (Trigger), não precisamos passar
        return pedidoRepository.save(novoPedido);
    }

    // 3. ATUALIZAR STATUS (UPDATE) - Novo Método
    @PutMapping("/{id}/entregar")
    public ResponseEntity<Pedido> marcarComoEntregue(@PathVariable Long id) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setStatus("ENTREGUE");
            Pedido atualizado = pedidoRepository.save(pedido);
            
            // Limpa o cache antigo do Redis para não mostrar dado velho
            // (Opcional, mas boa prática)
            // redisService.limparChave("status_pedido_" + id); 
            
            return ResponseEntity.ok(atualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. EXCLUIR (DELETE) - Novo Método
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPedido(@PathVariable Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoint de consulta individual (Já existia)
    @GetMapping("/{id}/status")
    public ResponseEntity<String> consultarStatus(@PathVariable Long id) {
        String chaveRedis = "status_pedido_" + id;
        String statusCache = redisService.buscarNoCache(chaveRedis);

        if (statusCache != null) return ResponseEntity.ok("Do Redis (Cache): " + statusCache);

        Optional<Pedido> pedidoBanco = pedidoRepository.findById(id);
        if (pedidoBanco.isPresent()) {
            String statusReal = pedidoBanco.get().getStatus();
            redisService.salvarNoCache(chaveRedis, statusReal);
            return ResponseEntity.ok("Do MySQL (Banco): " + statusReal);
        }
        return ResponseEntity.notFound().build();
    }

@GetMapping("/rastreio/{codigo}")
    public ResponseEntity<Pedido> buscarPorRastreio(@PathVariable String codigo) {
        // Usa o método novo que criamos no Repositório
        return pedidoRepository.findByCodigoRastreio(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}