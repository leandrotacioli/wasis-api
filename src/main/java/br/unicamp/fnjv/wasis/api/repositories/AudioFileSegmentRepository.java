package br.unicamp.fnjv.wasis.api.repositories;

import br.unicamp.fnjv.wasis.api.db.entities.AudioFile;
import br.unicamp.fnjv.wasis.api.db.entities.AudioFileSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioFileSegmentRepository extends JpaRepository<AudioFileSegment, Integer> {

    List<AudioFileSegment> findAllByAudioFile(AudioFile audioFile);

}