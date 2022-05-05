package br.unicamp.fnjv.wasis.api.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AudioFileDTO {

    private Integer idAudioFile;
    private String audioFileHash;
    private AnimalTaxonomyDTO animalTaxonomy;
    private LocalDateTime dateRecording;
    private String locationCity;
    private String locationState;
    private String locationCountry;
    private String recordist;
    private String observations;
    private List<AudioFileSegmentDTO> segments;

}