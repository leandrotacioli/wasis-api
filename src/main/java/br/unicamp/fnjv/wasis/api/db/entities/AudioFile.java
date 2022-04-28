package br.unicamp.fnjv.wasis.api.db.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "AudioFile")
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="IdAudioFile")
    private Integer idAudioFile;

    @Column(name="AudioFileHash", columnDefinition="CHAR(64)", nullable = false)
    private String audioFileHash;

    @ManyToOne
    @JoinColumn(name="IdAnimalTaxonomy")
    private AnimalTaxonomy animalTaxonomy;

    @Column(name="DateRecording")
    private LocalDateTime dateRecording;

    @Column(name="LocationCity", length = 50)
    private String locationCity;

    @Column(name="LocationState", length = 50)
    private String locationState;

    @Column(name="LocationCountry", length = 50)
    private String locationCountry;

    @Column(name="Recordist", length = 100)
    private String recordist;

    @Column(name="Observations", columnDefinition="TEXT")
    private String observations;

}