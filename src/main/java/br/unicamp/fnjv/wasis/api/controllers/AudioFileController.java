package br.unicamp.fnjv.wasis.api.controllers;

import br.unicamp.fnjv.wasis.api.db.entities.AudioFile;
import br.unicamp.fnjv.wasis.api.db.entities.AudioFileSegment;
import br.unicamp.fnjv.wasis.api.dtos.AudioFileDTO;
import br.unicamp.fnjv.wasis.api.dtos.AudioFileSegmentDTO;
import br.unicamp.fnjv.wasis.api.services.AudioFileSegmentService;
import br.unicamp.fnjv.wasis.api.services.AudioFileService;
import br.unicamp.fnjv.wasis.api.utils.api.ApiMapper;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/audio-files")
public class AudioFileController {

    @Autowired
    private AudioFileService audioFileService;

    @Autowired
    private AudioFileSegmentService audioFileSegmentService;

    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<AudioFileDTO> getAudioFile(@PathVariable Integer id) {
        Optional<AudioFile> audioFile = audioFileService.getAudioFile(id);

        if (audioFile.isEmpty()) {
            throw new GeneralException(HttpStatus.NOT_FOUND, "Arquivo de áudio não encontrado para o código informado.");
        }

        return new ResponseEntity<>(getAudioFileDTO(audioFile.get()), HttpStatus.OK);
    }

    @RequestMapping(value = "hash/{hash}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<AudioFileDTO> getAudioFile(@PathVariable String hash) {
        Optional<AudioFile> audioFile = audioFileService.getAudioFileByHash(hash);

        if (audioFile.isEmpty()) {
            throw new GeneralException(HttpStatus.NOT_FOUND, "Arquivo de áudio não encontrado para o hash informado.");
        }

        return new ResponseEntity<>(getAudioFileDTO(audioFile.get()), HttpStatus.OK);
    }

    /**
     * Cria a DTO para um registro de áudio do banco de dados.
     *
     * @param audioFile
     *
     * @return audioFileDTO
     */
    private AudioFileDTO getAudioFileDTO(AudioFile audioFile) {
        AudioFileDTO audioFileDTO = ApiMapper.map(audioFile, AudioFileDTO.class);

        // Retorna os segmentos do arquivo de áudio
        List<AudioFileSegment> audioFileSegments = audioFileSegmentService.getAudioFileSegments(audioFile);
        audioFileDTO.setSegments(ApiMapper.mapList(audioFileSegments, AudioFileSegmentDTO.class));

        return audioFileDTO;
    }

}