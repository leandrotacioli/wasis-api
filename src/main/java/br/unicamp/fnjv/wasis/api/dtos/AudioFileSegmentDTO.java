package br.unicamp.fnjv.wasis.api.dtos;

import lombok.Data;

@Data
public class AudioFileSegmentDTO {

    private Integer idAudioFileSegment;
    private AnimalTaxonomyDTO animalTaxonomy;
    private int timeInitial;
    private int timeFinal;
    private int frequencyInitial;
    private int frequencyFinal;

}