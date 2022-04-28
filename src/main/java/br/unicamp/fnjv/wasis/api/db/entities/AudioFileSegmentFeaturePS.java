package br.unicamp.fnjv.wasis.api.db.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "AudioFileSegmentFeaturePS")
public class AudioFileSegmentFeaturePS {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="IdAudioFileSegmentFeaturePS")
    private Integer idAudioFileSegmentFeaturePS;

    @ManyToOne
    @JoinColumn(name="IdAudioFileSegment")
    private AudioFileSegment audioFileSegment;

    @Column(name="VectorFrequency", columnDefinition="TEXT")
    private String vectorFrequency;

    @Column(name="VectorDecibel", columnDefinition="TEXT")
    private String vectorDecibel;

}