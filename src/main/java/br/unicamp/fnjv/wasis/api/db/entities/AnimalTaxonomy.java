package br.unicamp.fnjv.wasis.api.db.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "AnimalTaxonomy")
public class AnimalTaxonomy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="IdAnimalTaxonomy")
    private Integer idAnimalTaxonomy;

    @Column(name="PhylumRank", length = 50)
    private String phylumRank;

    @Column(name="ClassRank", length = 50)
    private String classRank;

    @Column(name="OrderRank", length = 50)
    private String orderRank;

    @Column(name="FamilyRank", length = 50)
    private String familyRank;

    @Column(name="GenusRank", length = 50)
    private String genusRank;

    @Column(name="SpeciesRank", length = 50)
    private String speciesRank;

    @Column(name="CommonNameEnglish", length = 100)
    private String commonNameEnglish;

    @Column(name="CommonNamePortuguese", length = 100)
    private String commonNamePortuguese;

}