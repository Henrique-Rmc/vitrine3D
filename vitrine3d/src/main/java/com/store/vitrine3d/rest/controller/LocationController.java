package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.repository.CityRepository;
import com.store.vitrine3d.domain.repository.StateRepository;
import com.store.vitrine3d.rest.dto.CityResponse;
import com.store.vitrine3d.rest.dto.StateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Localização", description = "Estados e cidades brasileiras para seleção no cadastro")
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    public LocationController(StateRepository stateRepository, CityRepository cityRepository) {
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
    }

    @Operation(summary = "Lista todos os estados brasileiros ordenados por nome")
    @GetMapping("/states")
    public List<StateResponse> listStates() {
        return stateRepository.findAll().stream()
                .map(StateResponse::from)
                .sorted(java.util.Comparator.comparing(StateResponse::getName))
                .toList();
    }

    @Operation(summary = "Lista cidades de um estado ordenadas por nome")
    @GetMapping("/states/{stateId}/cities")
    public List<CityResponse> listCitiesByState(@PathVariable Long stateId) {
        return cityRepository.findByStateIdOrderByNameAsc(stateId).stream()
                .map(CityResponse::from)
                .toList();
    }
}
