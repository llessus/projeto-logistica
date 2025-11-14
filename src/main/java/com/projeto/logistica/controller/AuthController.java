package com.projeto.logistica.controller;

import com.projeto.logistica.model.Usuario;
import com.projeto.logistica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> dadosLogin) {
        String email = dadosLogin.get("email");
        String senha = dadosLogin.get("senha");

        // 1. Busca o usuário no banco
        // (Isso vai funcionar agora que você arrumou o Repository)
        Usuario usuarioBanco = usuarioRepository.findByEmail(email);

        // 2. Verifica se existe e se a senha bate
        if (usuarioBanco != null && usuarioBanco.getSenha().equals(senha)) {

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("email", usuarioBanco.getEmail());
            resposta.put("nome", usuarioBanco.getNome());
            
            // --- AQUI ESTAVA O ERRO, AGORA ESTÁ CORRIGIDO ---
            // Usamos getIdGrupo() porque foi assim que definimos no Usuario.java
            if (usuarioBanco.getIdGrupo() != null && usuarioBanco.getIdGrupo() == 1) {
                resposta.put("role", "ROLE_ADMIN");
            } else {
                resposta.put("role", "ROLE_USER");
            }

            return ResponseEntity.ok(resposta);
        }

        // 3. Login falhou
        Map<String, Object> erro = new HashMap<>();
        erro.put("message", "Email ou senha incorretos!");
        return ResponseEntity.status(401).body(erro);
    }
}