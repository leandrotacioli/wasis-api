package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.repositories.AudioFileSegmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AudioFileSegmentService {

    @Autowired
    private AudioFileSegmentRepository audioFileSegmentRepository;

}