package br.unicamp.fnjv.wasis.api.dtos;

import lombok.Data;

@Data
public class AnimalTaxonomyDTO {

    private Integer idAnimalTaxonomy;
    private String phylumRank;
    private String classRank;
    private String orderRank;
    private String familyRank;
    private String genusRank;
    private String speciesRank;
    private String commonNameEnglish;
    private String commonNamePortuguese;

}