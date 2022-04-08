package br.unicamp.fnjv.wasis.api.repositories;

import br.unicamp.fnjv.wasis.api.db.entities.AudioLibrary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioLibraryRepository extends JpaRepository<AudioLibrary, Integer> {

}