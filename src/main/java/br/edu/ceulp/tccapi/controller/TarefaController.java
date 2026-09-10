package br.edu.ceulp.tccapi.controller;

import br.edu.ceulp.tccapi.model.Tarefa;
import br.edu.ceulp.tccapi.repository.TarefaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD basico de tarefas.
 *
 * IMPORTANTE (Entrega 1): este controller e o BASELINE proposital
 * sem seguranca. Qualquer requisicao pode criar, ler, alterar ou
 * excluir qualquer tarefa de qualquer usuario - inclusive tarefas
 * de outros "donos" (usuarioId). Isso sera usado na Entrega 2 para
 * rodar o OWASP ZAP e registrar as vulnerabilidades iniciais
 * (ex: ausencia de autenticacao, BOLA/API1:2023, etc).
 *
 * NAO adicionar Spring Security aqui ainda - isso e trabalho da
 * Entrega 3 em diante.
 */
@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaRepository repository;

    public TarefaController(TarefaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Tarefa> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tarefa criar(@Valid @RequestBody Tarefa tarefa) {
        return repository.save(tarefa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(@PathVariable Long id, @Valid @RequestBody Tarefa dados) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setTitulo(dados.getTitulo());
                    existente.setDescricao(dados.getDescricao());
                    existente.setStatus(dados.getStatus());
                    existente.setUsuarioId(dados.getUsuarioId());
                    return ResponseEntity.ok(repository.save(existente));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
