package com.projeto.logistica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Salva uma informação no Redis com validade de 5 minutos (TTL)
    public void salvarNoCache(String chave, String valor) {
        try {
            // Exemplo de chave: "pedido:1" -> Valor: "ENTREGUE"
            redisTemplate.opsForValue().set(chave, valor, 5, TimeUnit.MINUTES);
            System.out.println(">>> [REDIS] Salvo no cache: " + chave);
        } catch (Exception e) {
            System.out.println("!!! Erro ao salvar no Redis: " + e.getMessage());
        }
    }

    // Tenta buscar a informação no Redis
    public String buscarNoCache(String chave) {
        try {
            String valor = redisTemplate.opsForValue().get(chave);
            if (valor != null) {
                System.out.println(">>> [REDIS] Encontrado no cache (Rápido!): " + chave);
            } else {
                System.out.println(">>> [REDIS] Não achou no cache (Vai ter que ir no MySQL): " + chave);
            }
            return valor;
        } catch (Exception e) {
            return null; // Se der erro, finge que não achou e segue a vida
        }
    }
}