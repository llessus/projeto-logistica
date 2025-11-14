package com.projeto.logistica.repository;

import com.projeto.logistica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Essa é a linha que estava faltando para o Controller funcionar!
    Usuario findByEmail(String email);
    
}