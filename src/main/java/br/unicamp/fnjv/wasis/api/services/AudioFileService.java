package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.db.entities.AudioFile;
import br.unicamp.fnjv.wasis.api.repositories.AudioFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AudioFileService {

    @Autowired
    private AudioFileRepository audioFileRepository;

    public Optional<AudioFile> getAudioFile(Integer id) {
        return audioFileRepository.findById(id);
    }

    public Optional<AudioFile> getAudioFileByHash(String hash) {
        return audioFileRepository.findByAudioFileHash(hash);
    }

}