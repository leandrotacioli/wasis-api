package br.unicamp.fnjv.wasis.api.repositories;

import br.unicamp.fnjv.wasis.api.db.entities.AudioFileSegment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioFileSegmentRepository extends JpaRepository<AudioFileSegment, Integer> {

}