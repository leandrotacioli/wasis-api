package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.dtos.WasisInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WasisService {

    @Autowired
    private WasisInfoDTO wasisInfoDTO;

    public WasisInfoDTO getInfo() {
        return wasisInfoDTO;
    }

}