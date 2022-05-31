package br.unicamp.fnjv.wasis.api.repositories;

import br.unicamp.fnjv.wasis.api.db.entities.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AudioFileRepository extends JpaRepository<AudioFile, Integer> {

    Optional<AudioFile> findByAudioFileHash(String hash);

}