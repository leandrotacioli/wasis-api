package br.unicamp.fnjv.wasis.api.dtos;

import lombok.Data;

@Data
public class AudioDTO {

    private String hash;
    private String fileName;
    private String fileNameOriginal;
    private long durationMilliseconds;
    private String durationDigitalFormat;

}