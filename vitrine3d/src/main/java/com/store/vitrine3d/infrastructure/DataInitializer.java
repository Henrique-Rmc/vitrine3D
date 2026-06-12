package com.store.vitrine3d.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.City;
import com.store.vitrine3d.domain.model.State;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.repository.CityRepository;
import com.store.vitrine3d.domain.repository.StateRepository;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(CategoryRepository categoryRepository,
                           StateRepository stateRepository,
                           CityRepository cityRepository,
                           ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedCategories();
        seedStatesAndCities();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        List<String> globalNames = List.of(
                "Animes", "Pokemon", "Filmes e Séries", "Jogos",
                "Esculturas / Bustos", "Utensílios / Casa", "Fidget Toys / Articulados"
        );

        List<Category> globals = globalNames.stream().map(name -> {
            Category c = new Category();
            c.setName(name);
            c.setIsGlobal(true);
            return c;
        }).toList();

        categoryRepository.saveAll(new ArrayList<>(globals));
    }

    private void seedStatesAndCities() throws Exception {
        if (stateRepository.count() > 0) return;

        ClassPathResource resource = new ClassPathResource("states-cities.json");
        StateData[] stateDataArray = objectMapper.readValue(resource.getInputStream(), StateData[].class);

        for (StateData stateData : stateDataArray) {
            State state = stateRepository.save(new State(stateData.getName(), stateData.getAbbreviation()));

            List<City> cities = stateData.getCities().stream()
                    .map(cityName -> new City(cityName, state))
                    .toList();
            cityRepository.saveAll(new ArrayList<>(cities));
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class StateData {
        private String name;
        private String abbreviation;
        private List<String> cities;
    }
}
