package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.db.entities.AnimalTaxonomy;
import br.unicamp.fnjv.wasis.api.repositories.AnimalTaxonomyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnimalTaxonomyService {

    @Autowired
    private AnimalTaxonomyRepository animalTaxonomyRepository;

    public List<AnimalTaxonomy> listAnimalTaxonomies() {
        return animalTaxonomyRepository.findAll();
    }

    public Optional<AnimalTaxonomy> getAnimalTaxonomy(Integer id) {
        return animalTaxonomyRepository.findById(id);
    }

}