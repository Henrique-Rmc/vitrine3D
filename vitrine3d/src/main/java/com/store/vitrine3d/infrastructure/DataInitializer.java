package com.store.vitrine3d.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.City;
import com.store.vitrine3d.domain.model.AdminUser;
import com.store.vitrine3d.domain.model.PlanLimit;
import com.store.vitrine3d.domain.model.State;
import com.store.vitrine3d.domain.model.SubscriptionPlan;
import com.store.vitrine3d.domain.repository.AdminUserRepository;
import com.store.vitrine3d.domain.repository.BusinessTypeRepository;
import com.store.vitrine3d.domain.repository.CityRepository;
import com.store.vitrine3d.domain.repository.PlanLimitRepository;
import com.store.vitrine3d.domain.repository.StateRepository;
import lombok.Data;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final AdminUserRepository adminUserRepository;
    private final PlanLimitRepository planLimitRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(StateRepository stateRepository,
                           CityRepository cityRepository,
                           BusinessTypeRepository businessTypeRepository,
                           AdminUserRepository adminUserRepository,
                           PlanLimitRepository planLimitRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper) {
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.adminUserRepository = adminUserRepository;
        this.planLimitRepository = planLimitRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedStatesAndCities();
        seedBusinessTypes();
        seedAdmin();
        seedPlanLimits();
    }

    // DORMANT — seeds default limits per plan; enforcement is not yet wired.
    private void seedPlanLimits() {
        seedPlanLimit(SubscriptionPlan.FREE,  20,  3,  3, 3, false, false);
        seedPlanLimit(SubscriptionPlan.BASIC, 100, 10, 5, 3, true,  false);
        seedPlanLimit(SubscriptionPlan.PRO,   -1,  -1, 10, 5, true, true);
    }

    private void seedPlanLimit(SubscriptionPlan plan, int maxProducts, int maxProductTypes,
                                int maxFeatured, int maxPromo,
                                boolean affiliate, boolean customDomain) {
        if (planLimitRepository.existsByPlan(plan)) return;
        planLimitRepository.save(new PlanLimit(plan, maxProducts, maxProductTypes,
                maxFeatured, maxPromo, affiliate, customDomain));
    }

    private void seedAdmin() {
        if (adminUserRepository.existsByEmail(adminEmail)) return;
        AdminUser admin = new AdminUser();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setName("Admin");
        adminUserRepository.save(admin);
    }

    /**
     * Tipos de negocio sao so um rotulo que a loja escolhe no cadastro (tracking/analytics)
     * — nao carregam mais atributo nenhum. Quem monta o schema de atributos de cada loja e
     * o ProductType, criado pela propria loja sob demanda.
     */
    private void seedBusinessTypes() {
        seedBusinessType("Automóveis e Veículos", "automoveis");
        seedBusinessType("Imóveis", "imoveis");
        seedBusinessType("Moda e Vestuário", "moda-vestuario");
        seedBusinessType("Calçados", "calcados");
        seedBusinessType("Joias e Acessórios", "joias-acessorios");
        seedBusinessType("Beleza e Estética", "beleza-estetica");
        seedBusinessType("Saúde e Bem-estar", "saude-bem-estar");
        seedBusinessType("Eletrônicos e Informática", "eletronicos");
        seedBusinessType("Eletrodomésticos", "eletrodomesticos");
        seedBusinessType("Casa e Decoração", "casa-decoracao");
        seedBusinessType("Móveis", "moveis");
        seedBusinessType("Materiais de Construção", "materiais-construcao");
        seedBusinessType("Alimentos e Bebidas", "alimentos-bebidas");
        seedBusinessType("Pet Shop", "pet-shop");
        seedBusinessType("Livros e Papelaria", "livros-papelaria");
        seedBusinessType("Brinquedos e Jogos", "brinquedos-jogos");
        seedBusinessType("Geek e Colecionáveis", "geek-colecionaveis");
        seedBusinessType("Esportes e Fitness", "esportes-fitness");
        seedBusinessType("Instrumentos Musicais", "instrumentos-musicais");
        seedBusinessType("Arte e Artesanato", "arte-artesanato");
        seedBusinessType("Presentes e Papelaria", "presentes-papelaria");
        seedBusinessType("Serviços Profissionais", "servicos-profissionais");
        seedBusinessType("Consultórios e Clínicas", "consultorios-clinicas");
        seedBusinessType("Outro", "outro");
    }

    private void seedBusinessType(String name, String slug) {
        if (businessTypeRepository.existsBySlug(slug)) return;

        BusinessType businessType = new BusinessType();
        businessType.setName(name);
        businessType.setSlug(slug);
        businessTypeRepository.save(businessType);
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
