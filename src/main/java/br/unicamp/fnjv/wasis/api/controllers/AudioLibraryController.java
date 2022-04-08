package br.unicamp.fnjv.wasis.api.controllers;

import br.unicamp.fnjv.wasis.api.dtos.AudioLibraryDTO;
import br.unicamp.fnjv.wasis.api.services.AudioLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audio-libraries")
public class AudioLibraryController {

    @Autowired
    private AudioLibraryService audioLibraryService;

    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<AudioLibraryDTO>> listAudioLibraries() {
        return new ResponseEntity<>(audioLibraryService.listAudioLibraries(), HttpStatus.OK);
    }

}