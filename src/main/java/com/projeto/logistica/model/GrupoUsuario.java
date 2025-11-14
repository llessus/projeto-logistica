package com.projeto.logistica.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "grupos_usuarios")
public class GrupoUsuario {

    @Id
    private Integer id_grupo;

    private String nome_grupo;

    private String descricao;
}