package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.db.entities.AudioLibrary;
import br.unicamp.fnjv.wasis.api.db.entities.AudioLibraryFile;
import br.unicamp.fnjv.wasis.api.dtos.AudioLibraryDTO;
import br.unicamp.fnjv.wasis.api.dtos.AudioLibraryFileDTO;
import br.unicamp.fnjv.wasis.api.repositories.AudioLibraryFileRepository;
import br.unicamp.fnjv.wasis.api.repositories.AudioLibraryRepository;
import br.unicamp.fnjv.wasis.api.utils.api.ApiMapper;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AudioLibraryService {

    @Autowired
    private AudioLibraryRepository audioLibraryRepository;

    @Autowired
    private AudioLibraryFileRepository audioLibraryFileRepository;

    public List<AudioLibraryDTO> listAudioLibraries() {
        List<AudioLibrary> audioLibraries = audioLibraryRepository.findAll();

        if (audioLibraries.size() == 0) {
            throw new GeneralException(HttpStatus.NOT_FOUND, "Listagem de bibliotecas de áudio não encontrada.");
        }

        List<AudioLibraryDTO> audioLibrariesDTOs = new ArrayList<>();

        for (AudioLibrary audioLibrary : audioLibraries) {
            AudioLibraryDTO audioLibraryDTO = ApiMapper.map(audioLibrary, AudioLibraryDTO.class);

            List<AudioLibraryFile> audioLibraryFiles = audioLibraryFileRepository.findAllByAudioLibrary(audioLibrary);
            audioLibraryDTO.setFiles(ApiMapper.mapList(audioLibraryFiles, AudioLibraryFileDTO.class));

            audioLibrariesDTOs.add(audioLibraryDTO);
        }

        return audioLibrariesDTOs;
    }

}