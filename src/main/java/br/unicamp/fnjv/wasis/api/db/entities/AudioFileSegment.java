package br.unicamp.fnjv.wasis.api.db.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "AudioFileSegment")
public class AudioFileSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="IdAudioFileSegment")
    private Integer idAudioFileSegment;

    @ManyToOne
    @JoinColumn(name="IdAudioFile")
    private AudioFile audioFile;

    @ManyToOne
    @JoinColumn(name="IdAnimalTaxonomy")
    private AnimalTaxonomy animalTaxonomy;

    @Column(name="TimeInitial")
    private int timeInitial;

    @Column(name="TimeFinal")
    private int timeFinal;

    @Column(name="FrequencyInitial")
    private int frequencyInitial;

    @Column(name="FrequencyFinal")
    private int frequencyFinal;

}