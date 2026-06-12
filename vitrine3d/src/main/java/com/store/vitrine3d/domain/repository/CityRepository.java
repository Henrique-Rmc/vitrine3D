package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByStateIdOrderByNameAsc(Long stateId);
}
