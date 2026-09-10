package br.edu.ceulp.tccapi.repository;

import br.edu.ceulp.tccapi.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
}
