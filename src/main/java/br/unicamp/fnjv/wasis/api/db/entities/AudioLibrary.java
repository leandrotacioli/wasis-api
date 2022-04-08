package br.unicamp.fnjv.wasis.api.db.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "AudioLibrary")
public class AudioLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="IdAudioLibrary")
    private Integer idAudioLibrary;

    @Column(name="LibraryName", length = 50, nullable = false)
    private String libraryName;

    @Column(name="LibraryDescription", length = 200)
    private String libraryDescription;

    @Column(name="LibraryObservation", columnDefinition="TEXT")
    private String libraryObservation;

}