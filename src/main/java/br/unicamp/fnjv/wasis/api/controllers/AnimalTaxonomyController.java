package br.unicamp.fnjv.wasis.api.controllers;

import br.unicamp.fnjv.wasis.api.db.entities.AnimalTaxonomy;
import br.unicamp.fnjv.wasis.api.dtos.AnimalTaxonomyDTO;
import br.unicamp.fnjv.wasis.api.services.AnimalTaxonomyService;
import br.unicamp.fnjv.wasis.api.utils.api.ApiMapper;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/animal-taxonomies")
public class AnimalTaxonomyController {

    @Autowired
    private AnimalTaxonomyService animalTaxonomyService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<AnimalTaxonomyDTO>> listAnimalTaxonomies() {
        List<AnimalTaxonomy> animalTaxonomies = animalTaxonomyService.listAnimalTaxonomies();

        if (animalTaxonomies.size() == 0) {
            throw new GeneralException(HttpStatus.NOT_FOUND, "Listagem de taxonomia não encontrada.");
        }

        return new ResponseEntity<>(ApiMapper.mapList(animalTaxonomies, AnimalTaxonomyDTO.class), HttpStatus.OK);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<AnimalTaxonomyDTO> getAnimalTaxonomy(@PathVariable Integer id) {
        Optional<AnimalTaxonomy> animalTaxonomy = animalTaxonomyService.getAnimalTaxonomy(id);

        if (animalTaxonomy.isEmpty()) {
            throw new GeneralException(HttpStatus.NOT_FOUND, "Taxonomia não encontrada para o código informado.");
        }

        return new ResponseEntity<>(ApiMapper.map(animalTaxonomy, AnimalTaxonomyDTO.class), HttpStatus.OK);
    }

}