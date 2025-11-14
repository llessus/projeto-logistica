package com.projeto.logistica.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;

    private String nome;

    @Column(unique = true) // Garante que não existam 2 e-mails iguais
    private String email;

    private String senha;

    // Estamos mapeando apenas o ID do grupo (1 ou 2) para facilitar seu login
    @Column(name = "id_grupo")
    private Integer idGrupo;

    // --- Getters e Setters (Obrigatórios para o Controller pegar os dados) ---

    public Long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Integer idGrupo) {
        this.idGrupo = idGrupo;
    }
}