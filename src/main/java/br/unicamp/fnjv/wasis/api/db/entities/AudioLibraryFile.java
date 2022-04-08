package br.unicamp.fnjv.wasis.api.db.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "AudioLibraryFile")
public class AudioLibraryFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="IdAudioLibraryFile")
    private Integer idAudioLibraryFile;

    @ManyToOne
    @JoinColumn(name="IdAudioLibrary")
    private AudioLibrary audioLibrary;

    @Column(name="AudioFileHash", columnDefinition="CHAR(64)")
    private String audioFileHash;

    @Column(name="AudioFileName", length = 200, nullable = false)
    private String audioFileName;

}