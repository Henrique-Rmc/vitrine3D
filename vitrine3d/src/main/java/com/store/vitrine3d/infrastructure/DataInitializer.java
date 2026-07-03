package com.store.vitrine3d.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.City;
import com.store.vitrine3d.domain.model.State;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.repository.CityRepository;
import com.store.vitrine3d.domain.repository.StateRepository;
import lombok.Data;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O CommandLineRunner só executa em runtime de verdade, nunca durante o Spring AOT — então o
 * GraalVM não descobre sozinho que estados-cidades2.json precisa estar embutido no binário. Sem
 * esse hint, o arquivo só "falta" quando o banco está vazio (primeiro boot contra schema novo).
 */
@Component
@ImportRuntimeHints(DataInitializer.SeedResourceHints.class)
public class DataInitializer implements CommandLineRunner {

    // IBGE state code → UF abbreviation (not present in the JSON file)
    private static final Map<Integer, String> IBGE_SIGLA = Map.ofEntries(
            Map.entry(11, "RO"), Map.entry(12, "AC"), Map.entry(13, "AM"),
            Map.entry(14, "RR"), Map.entry(15, "PA"), Map.entry(16, "AP"),
            Map.entry(17, "TO"), Map.entry(21, "MA"), Map.entry(22, "PI"),
            Map.entry(23, "CE"), Map.entry(24, "RN"), Map.entry(25, "PB"),
            Map.entry(26, "PE"), Map.entry(27, "AL"), Map.entry(28, "SE"),
            Map.entry(29, "BA"), Map.entry(31, "MG"), Map.entry(32, "ES"),
            Map.entry(33, "RJ"), Map.entry(35, "SP"), Map.entry(41, "PR"),
            Map.entry(42, "SC"), Map.entry(43, "RS"), Map.entry(50, "MS"),
            Map.entry(51, "MT"), Map.entry(52, "GO"), Map.entry(53, "DF")
    );

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

        ClassPathResource resource = new ClassPathResource("estados-cidades2.json");
        LocationData data = objectMapper.readValue(resource.getInputStream(), LocationData.class);

        // Save states and keep IBGE code → entity mapping for city lookup
        Map<Integer, State> stateByIbge = new HashMap<>();
        for (Map.Entry<String, String> entry : data.getStates().entrySet()) {
            int ibgeCode = Integer.parseInt(entry.getKey());
            String sigla = IBGE_SIGLA.getOrDefault(ibgeCode, "??");
            State state = stateRepository.save(new State(entry.getValue(), sigla));
            stateByIbge.put(ibgeCode, state);
        }

        // Flat city list — each entry carries its own state_id reference
        List<City> cities = data.getCities().stream()
                .filter(cd -> stateByIbge.containsKey(cd.getStateId()))
                .map(cd -> new City(cd.getName(), stateByIbge.get(cd.getStateId())))
                .toList();

        cityRepository.saveAll(new ArrayList<>(cities));
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class LocationData {
        private Map<String, String> states;
        private List<CityData> cities;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class CityData {
        @JsonProperty("state_id")
        private int stateId;
        private int id;
        private String name;
    }

    static class SeedResourceHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern("estados-cidades2.json");
            hints.reflection().registerType(LocationData.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS);
            hints.reflection().registerType(CityData.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS);
        }
    }
}
