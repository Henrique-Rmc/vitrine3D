package com.store.vitrine3d.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.City;
import com.store.vitrine3d.domain.model.State;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.BusinessTypeRepository;
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

    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(StateRepository stateRepository,
                           CityRepository cityRepository,
                           BusinessTypeRepository businessTypeRepository,
                           AttributeDefinitionRepository attributeDefinitionRepository,
                           ObjectMapper objectMapper) {
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedStatesAndCities();
        seedBusinessTypes();
    }

    private void seedBusinessTypes() {
        seedBusinessType("Automóveis", "automoveis", List.of(
                textAttr("marca", "Marca", true, 0),
                numberAttr("ano", "Ano", null, true, 1),
                numberAttr("kilometragem", "Quilometragem", "km", false, 2)
        ));

        seedBusinessType("Imóveis", "imoveis", List.of(
                enumAttr("tipoImovel", "Tipo de imóvel", true, 0),
                numberAttr("area", "Área", "m²", true, 1),
                numberAttr("quartos", "Quartos", null, false, 2)
        ));

        seedBusinessType("Roupas", "roupas", List.of(
                enumAttr("tamanho", "Tamanho", true, 0),
                textAttr("cor", "Cor", false, 1)
        ));

        seedBusinessType("Arte", "arte", List.of(
                textAttr("tecnica", "Técnica", true, 0),
                numberAttr("anoCriacao", "Ano de criação", null, false, 1)
        ));

        seedBusinessType("Alimentos", "alimentos", List.of(
                enumAttr("tipo", "Tipo", true, 0),
                dateAttr("validade", "Validade", false, 1)
        ));

        seedBusinessType("Consultórios Médicos", "medicos", List.of(
                enumAttr("especialidade", "Especialidade", true, 0),
                numberAttr("duracaoMinutos", "Duração", "min", true, 1),
                enumAttr("modalidade", "Modalidade", true, 2),
                booleanAttr("atendePorConvenio", "Atende por convênio", false, 3),
                enumAttr("publicoAlvo", "Público-alvo", false, 4)
        ));

        seedBusinessType("Beleza e Estética", "beleza-estetica", List.of(
                enumAttr("categoriaServico", "Categoria do serviço", true, 0),
                numberAttr("duracaoMinutos", "Duração", "min", false, 1),
                enumAttr("publicoAlvo", "Público-alvo", true, 2),
                booleanAttr("atendeDomicilio", "Atende a domicílio", false, 3),
                textAttr("profissionalResponsavel", "Profissional responsável", false, 4)
        ));

        seedBusinessType("Eletrônicos e Informática", "eletronicos", List.of(
                textAttr("marca", "Marca", true, 0),
                enumAttr("condicao", "Condição", true, 1),
                numberAttr("garantiaMeses", "Garantia", "meses", false, 2),
                enumAttr("voltagem", "Voltagem", false, 3),
                textAttr("conectividade", "Conectividade", false, 4)
        ));

        seedBusinessType("Eletrodomésticos", "eletrodomesticos", List.of(
                textAttr("marca", "Marca", true, 0),
                enumAttr("voltagem", "Voltagem", true, 1),
                numberAttr("garantiaMeses", "Garantia", "meses", false, 2),
                enumAttr("seloEnergetico", "Selo energético", false, 3),
                enumAttr("condicao", "Condição", false, 4)
        ));

        seedBusinessType("Móveis e Decoração", "moveis-decoracao", List.of(
                enumAttr("material", "Material", true, 0),
                textAttr("dimensoes", "Dimensões", false, 1),
                textAttr("cor", "Cor", false, 2),
                booleanAttr("montagemInclusa", "Montagem inclusa", false, 3),
                enumAttr("ambiente", "Ambiente", false, 4)
        ));

        seedBusinessType("Pet Shop", "pet-shop", List.of(
                enumAttr("especiePet", "Espécie", true, 0),
                enumAttr("porte", "Porte", false, 1),
                textAttr("marca", "Marca", false, 2),
                enumAttr("faixaEtaria", "Faixa etária", false, 3),
                numberAttr("pesoKg", "Peso", "kg", false, 4)
        ));

        seedBusinessType("Livros e Papelaria", "livros-papelaria", List.of(
                enumAttr("genero", "Gênero", true, 0),
                textAttr("autor", "Autor", false, 1),
                enumAttr("idioma", "Idioma", false, 2),
                enumAttr("condicao", "Condição", true, 3),
                numberAttr("numeroPaginas", "Número de páginas", null, false, 4)
        ));

        seedBusinessType("Esportes e Fitness", "esportes-fitness", List.of(
                enumAttr("modalidade", "Modalidade", true, 0),
                textAttr("tamanho", "Tamanho", false, 1),
                textAttr("marca", "Marca", false, 2),
                enumAttr("genero", "Gênero", false, 3),
                textAttr("material", "Material", false, 4)
        ));

        seedBusinessType("Brinquedos e Jogos", "brinquedos-jogos", List.of(
                enumAttr("faixaEtaria", "Faixa etária", true, 0),
                textAttr("marca", "Marca", false, 1),
                enumAttr("material", "Material", false, 2),
                textAttr("numeroJogadores", "Número de jogadores", false, 3),
                booleanAttr("pilhaIncluida", "Pilha inclusa", false, 4)
        ));

        seedBusinessType("Joias e Acessórios", "joias-acessorios", List.of(
                enumAttr("material", "Material", true, 0),
                textAttr("pedra", "Pedra", false, 1),
                enumAttr("genero", "Gênero", false, 2),
                numberAttr("garantiaMeses", "Garantia", "meses", false, 3),
                booleanAttr("banhoOuro", "Banho de ouro", false, 4)
        ));

        seedBusinessType("Materiais de Construção", "materiais-construcao", List.of(
                enumAttr("categoria", "Categoria", true, 0),
                textAttr("marca", "Marca", false, 1),
                enumAttr("unidadeVenda", "Unidade de venda", true, 2),
                numberAttr("garantiaMeses", "Garantia", "meses", false, 3),
                booleanAttr("certificadoInmetro", "Certificado Inmetro", false, 4)
        ));

        seedBusinessType("Instrumentos Musicais", "instrumentos-musicais", List.of(
                enumAttr("tipoInstrumento", "Tipo de instrumento", true, 0),
                textAttr("marca", "Marca", false, 1),
                enumAttr("condicao", "Condição", true, 2),
                booleanAttr("acompanhaCase", "Acompanha case", false, 3),
                enumAttr("nivelExperiencia", "Nível de experiência", false, 4)
        ));

        seedBusinessType("Bebidas", "bebidas", List.of(
                enumAttr("categoria", "Categoria", true, 0),
                textAttr("tipoBebida", "Tipo de bebida", false, 1),
                numberAttr("volumeMl", "Volume", "ml", true, 2),
                numberAttr("teorAlcoolico", "Teor alcoólico", "%", false, 3),
                textAttr("origem", "Origem", false, 4)
        ));

        seedBusinessType("Cosméticos e Perfumaria", "cosmeticos-perfumaria", List.of(
                enumAttr("categoria", "Categoria", true, 0),
                textAttr("marca", "Marca", true, 1),
                numberAttr("volumeMl", "Volume", "ml", false, 2),
                enumAttr("tipoPele", "Tipo de pele", false, 3),
                dateAttr("validade", "Validade", false, 4)
        ));

        seedBusinessType("Calçados", "calcados", List.of(
                numberAttr("numeracao", "Numeração", null, true, 0),
                enumAttr("genero", "Gênero", true, 1),
                enumAttr("material", "Material", false, 2),
                textAttr("marca", "Marca", false, 3),
                enumAttr("tipoCalcado", "Tipo de calçado", false, 4)
        ));
    }

    private void seedBusinessType(String name, String slug, List<AttributeDefinition> attributes) {
        if (businessTypeRepository.existsBySlug(slug)) return;

        BusinessType businessType = new BusinessType();
        businessType.setName(name);
        businessType.setSlug(slug);
        businessType = businessTypeRepository.save(businessType);

        for (AttributeDefinition attribute : attributes) {
            attribute.setBusinessType(businessType);
        }
        attributeDefinitionRepository.saveAll(attributes);
    }

    private AttributeDefinition textAttr(String key, String label, boolean required, int sortOrder) {
        return baseAttr(key, label, AttributeType.TEXT, required, sortOrder);
    }

    private AttributeDefinition numberAttr(String key, String label, String unit, boolean required, int sortOrder) {
        AttributeDefinition definition = baseAttr(key, label, AttributeType.NUMBER, required, sortOrder);
        definition.setUnit(unit);
        return definition;
    }

    private AttributeDefinition dateAttr(String key, String label, boolean required, int sortOrder) {
        return baseAttr(key, label, AttributeType.DATE, required, sortOrder);
    }

    private AttributeDefinition booleanAttr(String key, String label, boolean required, int sortOrder) {
        return baseAttr(key, label, AttributeType.BOOLEAN, required, sortOrder);
    }

    private AttributeDefinition enumAttr(String key, String label, boolean required, int sortOrder) {
        return baseAttr(key, label, AttributeType.ENUM, required, sortOrder);
    }

    private AttributeDefinition baseAttr(String key, String label, AttributeType type,
                                          boolean required, int sortOrder) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey(key);
        definition.setLabel(label);
        definition.setType(type);
        definition.setRequired(required);
        definition.setFilterable(true);
        definition.setSortOrder(sortOrder);
        return definition;
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
