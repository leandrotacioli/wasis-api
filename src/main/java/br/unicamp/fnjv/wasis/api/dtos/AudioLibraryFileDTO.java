package br.unicamp.fnjv.wasis.api.dtos;

import lombok.Data;

@Data
public class AudioLibraryFileDTO {

    private Integer idAudioLibraryFile;
    private String audioFileHash;
    private String audioFileName;

}