package com.store.vitrine3d.domain.model;

import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confirma que o mapeamento Product.attributes (JSON via Hibernate @JdbcTypeCode(SqlTypes.JSON))
 * grava e relê corretamente no H2 (usado nos testes) da mesma forma que no Postgres (producao).
 *
 * A consulta por caminho JSON (jsonb_extract_path_text) e especifica do Postgres — H2 nao tem
 * essa funcao — entao os predicados dinamicos de atributo em ProductSpec nao sao exercitados
 * aqui; foram validados manualmente contra o Postgres local (docker) antes de serem escritos.
 */
@DataJpaTest
class ProductAttributesJsonRepositoryTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private StoreRepository storeRepository;

    @Test
    void savesAndReloadsJsonAttributes() {
        Product saved = persistProduct("Corolla", Map.of("marca", "Toyota", "ano", 2020, "kilometragem", 45000));

        Product reloaded = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getAttributes())
                .containsEntry("marca", "Toyota")
                .containsEntry("ano", 2020);
    }

    private Product persistProduct(String name, Map<String, Object> attributes) {
        Store store = new Store();
        store.setEmail(name.toLowerCase() + "@test.com");
        store.setPassword("hash");
        store.setUserName("owner-" + name);
        store.setStoreName("Loja " + name);
        store.setWhatsappNumber("5511999999999");
        store = storeRepository.save(store);

        Product product = new Product();
        product.setName(name);
        product.setStore(store);
        product.setAttributes(attributes);
        return productRepository.save(product);
    }
}
