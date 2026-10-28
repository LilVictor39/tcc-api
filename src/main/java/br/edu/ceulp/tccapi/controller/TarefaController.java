package br.edu.ceulp.tccapi.controller;
import br.edu.ceulp.tccapi.model.Tarefa;
import br.edu.ceulp.tccapi.repository.TarefaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@RestController @RequestMapping("/api/tarefas")
public class TarefaController {
 private final TarefaRepository repository;
 public TarefaController(TarefaRepository r){repository=r;}
 // DTO nao permite escolher o dono nem alterar id e data de criacao.
 public record Dados(@NotBlank @Size(max=255) String titulo,@Size(max=1000) String descricao,@NotNull Tarefa.StatusTarefa status){}
 private Long dono(Jwt jwt){try{return Long.valueOf(jwt.getSubject());}catch(RuntimeException e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);}}
 private Tarefa obter(Long id,Long dono){return repository.findByIdAndUsuarioId(id,dono).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @GetMapping public List<Tarefa> listar(@AuthenticationPrincipal Jwt jwt){return repository.findAllByUsuarioId(dono(jwt));}
 @GetMapping("/{id}") public Tarefa buscar(@PathVariable @Positive Long id,@AuthenticationPrincipal Jwt jwt){return obter(id,dono(jwt));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Tarefa criar(@Valid @RequestBody Dados dados,@AuthenticationPrincipal Jwt jwt){
  var t=new Tarefa(dados.titulo(),dados.descricao(),dono(jwt));t.setStatus(dados.status());return repository.save(t);
 }
 @PutMapping("/{id}") @Transactional public Tarefa atualizar(@PathVariable @Positive Long id,@Valid @RequestBody Dados dados,@AuthenticationPrincipal Jwt jwt){
  var t=obter(id,dono(jwt));t.setTitulo(dados.titulo());t.setDescricao(dados.descricao());t.setStatus(dados.status());return repository.save(t);
 }
 @DeleteMapping("/{id}") @Transactional public ResponseEntity<Void> excluir(@PathVariable @Positive Long id,@AuthenticationPrincipal Jwt jwt){repository.delete(obter(id,dono(jwt)));return ResponseEntity.noContent().build();}
}
