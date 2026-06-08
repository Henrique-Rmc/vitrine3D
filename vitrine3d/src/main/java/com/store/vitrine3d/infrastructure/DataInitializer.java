package com.store.vitrine3d.infrastructure;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.repository.CategoryRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;

        List<String> globalNames = List.of(
                "Animes",
                "Pokemon",
                "Filmes e Séries",
                "Jogos",
                "Esculturas / Bustos",
                "Utensílios / Casa",
                "Fidget Toys / Articulados"
        );

        List<Category> globals = globalNames.stream().map(name -> {
            Category c = new Category();
            c.setName(name);
            c.setIsGlobal(true);
            return c;
        }).toList();

        categoryRepository.saveAll(new java.util.ArrayList<>(globals));
    }
}
