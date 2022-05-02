package br.unicamp.fnjv.wasis.api.repositories;

import br.unicamp.fnjv.wasis.api.db.entities.AnimalTaxonomy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalTaxonomyRepository extends JpaRepository<AnimalTaxonomy, Integer> {

}