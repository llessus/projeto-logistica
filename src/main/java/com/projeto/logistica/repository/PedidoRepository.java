package com.projeto.logistica.repository;

import com.projeto.logistica.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // <--- Importante para evitar erro se não achar o pedido

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    // O Spring cria o SQL sozinho ("SELECT * FROM pedidos WHERE codigo_rastreio = ?")
    // baseado apenas no nome deste método:
    Optional<Pedido> findByCodigoRastreio(String codigoRastreio);
    
}