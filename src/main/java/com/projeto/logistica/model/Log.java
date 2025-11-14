package com.projeto.logistica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log") // Importante: No banco chama id_log
    private Long id;

    private String acao;

    private String usuario;

    @Column(name = "data_log") // Importante: No banco tem underline
    private LocalDateTime dataLog;

    // --- Getters e Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getDataLog() { return dataLog; }
    public void setDataLog(LocalDateTime dataLog) { this.dataLog = dataLog; }
}