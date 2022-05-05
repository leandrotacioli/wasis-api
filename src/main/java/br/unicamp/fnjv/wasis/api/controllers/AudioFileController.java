package br.unicamp.fnjv.wasis.api.controllers;

import br.unicamp.fnjv.wasis.api.services.AudioFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audio-files")
public class AudioFileController {

    @Autowired
    private AudioFileService audioFileService;

}