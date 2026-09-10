package br.edu.ceulp.tccapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Entidade de dominio para o baseline da API (Entrega 1).
 * Propositalmente simples: o objetivo desta etapa e ter um CRUD
 * funcional SEM nenhum mecanismo de seguranca, servindo de base
 * de comparacao (baseline) para as entregas seguintes.
 *
 * Quando a autenticacao/autorizacao for implementada (Entrega 3),
 * o campo "usuarioId" (dono da tarefa) sera usado para o controle
 * de acesso a nivel de objeto (mitigacao de BOLA - OWASP API1:2023).
 */
@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status = StatusTarefa.PENDENTE;

    // Campo preparado para a fase de autorizacao (Entrega 3).
    // Por enquanto fica solto, sem FK para usuario/autenticacao.
    private Long usuarioId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    public enum StatusTarefa {
        PENDENTE, EM_ANDAMENTO, CONCLUIDA
    }

    public Tarefa() {
    }

    public Tarefa(String titulo, String descricao, Long usuarioId) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.usuarioId = usuarioId;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}
