package br.unicamp.fnjv.wasis.api.dtos;

import lombok.Data;

import java.util.List;

@Data
public class AudioLibraryDTO {

    private Integer idAudioLibrary;
    private String libraryName;
    private String libraryDescription;
    private String libraryObservation;

    private List<AudioLibraryFileDTO> files;

}