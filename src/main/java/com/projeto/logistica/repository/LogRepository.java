package com.projeto.logistica.repository;

import com.projeto.logistica.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    // Não precisa escrever nada aqui, o JpaRepository já faz tudo
}