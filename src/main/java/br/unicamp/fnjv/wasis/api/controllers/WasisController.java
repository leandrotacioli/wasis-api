package br.unicamp.fnjv.wasis.api.controllers;

import br.unicamp.fnjv.wasis.api.dtos.WasisInfoDTO;
import br.unicamp.fnjv.wasis.api.services.WasisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wasis")
public class WasisController {

    @Autowired
    private WasisService wasisService;

    @RequestMapping(value = "info", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<WasisInfoDTO> getInfo() {
        return new ResponseEntity<>(wasisService.getInfo(), HttpStatus.OK);
    }

}