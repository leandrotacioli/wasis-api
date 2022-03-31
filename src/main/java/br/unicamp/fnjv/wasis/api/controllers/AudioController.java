package br.unicamp.fnjv.wasis.api.controllers;

import br.unicamp.fnjv.wasis.api.services.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AudioService audioService;

    @RequestMapping(value = "load", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity loadAudio(@RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(audioService.loadAudio(file), HttpStatus.OK);
    }

}