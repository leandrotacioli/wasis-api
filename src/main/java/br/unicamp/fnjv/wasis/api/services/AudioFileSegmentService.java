package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.db.entities.AudioFile;
import br.unicamp.fnjv.wasis.api.db.entities.AudioFileSegment;
import br.unicamp.fnjv.wasis.api.repositories.AudioFileSegmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AudioFileSegmentService {

    @Autowired
    private AudioFileSegmentRepository audioFileSegmentRepository;

    public List<AudioFileSegment> getAudioFileSegments(AudioFile audioFile) {
        return audioFileSegmentRepository.findAllByAudioFile(audioFile);
    }

}