package com.projeto.logistica.controller;

// --- IMPORTS OBRIGATÓRIOS ---
import com.projeto.logistica.model.Pedido;
import com.projeto.logistica.repository.PedidoRepository;
import com.projeto.logistica.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // <-- Essencial para o findById
// ------------------------------

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

    // 2. CRIAR (CREATE)
    @PostMapping
    public Pedido criarPedido(@RequestBody Pedido novoPedido) {
        novoPedido.setDataPedido(LocalDateTime.now());
        novoPedido.setStatus("PENDENTE");
        return pedidoRepository.save(novoPedido);
    }

    // --- CORRIGIDO: Lógica reescrita para ser mais clara ---
    @PutMapping("/{id}/em-rota")
    public ResponseEntity<Pedido> marcarEmRota(@PathVariable Long id) {
        
        // 1. Busca o pedido no banco
        Optional<Pedido> optionalPedido = pedidoRepository.findById(id);

        // 2. Verifica se o pedido existe
        if (optionalPedido.isEmpty()) {
            return ResponseEntity.notFound().build(); // Retorna 404
        }

        // 3. Se existe, pega o pedido e checa o status
        Pedido pedido = optionalPedido.get();
        if (pedido.getStatus().equals("PENDENTE")) {
            pedido.setStatus("EM ROTA");
            Pedido atualizado = pedidoRepository.save(pedido);
            return ResponseEntity.ok(atualizado); // Retorna 200 OK com o pedido
        }

        // 4. Se já foi entregue ou já está em rota, retorna erro
        return ResponseEntity.badRequest().build(); // Retorna 400 Bad Request
    }

    // --- CORRIGIDO: Lógica reescrita para ser mais clara ---
    @PutMapping("/{id}/entregar")
    public ResponseEntity<Pedido> marcarComoEntregue(@PathVariable Long id) {
        
        // 1. Busca o pedido
        Optional<Pedido> optionalPedido = pedidoRepository.findById(id);

        // 2. Verifica se existe
        if (optionalPedido.isEmpty()) {
            return ResponseEntity.notFound().build(); // Retorna 404
        }

        // 3. Pega o pedido e checa o status
        Pedido pedido = optionalPedido.get();
        if (pedido.getStatus().equals("EM ROTA")) {
            pedido.setStatus("ENTREGUE");
            Pedido atualizado = pedidoRepository.save(pedido);
            return ResponseEntity.ok(atualizado); // Retorna 200 OK
        }

        // 4. Se ainda estiver PENDENTE (não pode pular etapa)
        return ResponseEntity.badRequest().build(); // Retorna 400 Bad Request
    }

    // 5. EXCLUIR (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPedido(@PathVariable Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 6. RASTREIO PÚBLICO (Também corrigido)
    @GetMapping("/rastreio/{codigo}")
    public ResponseEntity<Pedido> buscarPorRastreio(@PathVariable String codigo) {
        Optional<Pedido> pedido = pedidoRepository.findByCodigoRastreio(codigo);
        
        if (pedido.isPresent()) {
            return ResponseEntity.ok(pedido.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 7. CONSULTA STATUS (CACHE REDIS)
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
}