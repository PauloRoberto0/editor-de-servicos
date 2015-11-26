package br.gov.servicos.editor.usuarios;

import org.springframework.data.repository.Repository;

public interface PapelRepository extends Repository<Papel, Long> {
    Iterable<Papel> findAll();
}