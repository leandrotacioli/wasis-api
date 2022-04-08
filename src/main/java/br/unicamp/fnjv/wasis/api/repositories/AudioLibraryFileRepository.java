package br.unicamp.fnjv.wasis.api.repositories;

import br.unicamp.fnjv.wasis.api.db.entities.AudioLibrary;
import br.unicamp.fnjv.wasis.api.db.entities.AudioLibraryFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioLibraryFileRepository extends JpaRepository<AudioLibraryFile, Integer> {

    List<AudioLibraryFile> findAllByAudioLibrary(AudioLibrary audioLibrary);

}