package br.edu.ceulp.tccapi.repository;

import br.edu.ceulp.tccapi.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
 List<Tarefa> findAllByUsuarioId(Long usuarioId);
 Optional<Tarefa> findByIdAndUsuarioId(Long id,Long usuarioId);
}
