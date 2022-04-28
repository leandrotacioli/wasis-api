package br.unicamp.fnjv.wasis.api.db.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "AudioFileSegmentFeature")
public class AudioFileSegmentFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="IdAudioFileSegmentFeature")
    private Integer idAudioFileSegmentFeature;

    @ManyToOne
    @JoinColumn(name="IdAudioFileSegment")
    private AudioFileSegment audioFileSegment;

    @Column(name="Frame")
    private int frame;

    @Column(name="VectorMFCC", columnDefinition="TEXT")
    private String vectorMFCC;

    @Column(name="VectorLPC", columnDefinition="TEXT")
    private String vectorLPC;

    @Column(name="VectorLPCC", columnDefinition="TEXT")
    private String vectorLPCC;

    @Column(name="VectorPLP", columnDefinition="TEXT")
    private String vectorPLP;

    @Column(name="FlagNormalized")
    private boolean flagNormalized;

}