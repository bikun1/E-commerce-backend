package com.example.ecommerce.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.ecommerce.backend.entity.Category;
import com.example.ecommerce.backend.entity.Product;
import com.example.ecommerce.backend.entity.Role;
import com.example.ecommerce.backend.entity.User;
import com.example.ecommerce.backend.repository.CategoryRepository;
import com.example.ecommerce.backend.repository.ProductRepository;
import com.example.ecommerce.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        Map<String, Category> categories = seedCategories();
        seedProducts(categories);
    }

    // -------------------------------------------------------------------------
    // Users
    // -------------------------------------------------------------------------

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.debug("Users already seeded, skipping.");
            return;
        }

        List<User> users = List.of(
                buildUser("tuwu", "tuwu@example.com", "Tú Wu", Role.ROLE_ADMIN, Role.ROLE_USER),
                buildUser("bikun", "bikun@example.com", "Bikun", Role.ROLE_USER));

        userRepository.saveAll(users);
        log.info("Seeded {} users.", users.size());
    }

    private User buildUser(String username, String email, String fullName, Role... roles) {
        User user = new User(username, email, passwordEncoder.encode("123456"));
        user.setFullName(fullName);
        user.getRoles().clear();
        user.getRoles().addAll(List.of(roles));
        return user;
    }

    // -------------------------------------------------------------------------
    // Categories
    // -------------------------------------------------------------------------

    private Map<String, Category> seedCategories() {
        if (categoryRepository.count() > 0) {
            log.debug("Categories already seeded, loading existing.");
            return loadCategoriesByName();
        }

        List<Category> categories = List.of(
                buildCategory("Electronics", "Electronic devices and gadgets"),
                buildCategory("Books", "Books and magazines"),
                buildCategory("Fashion", "Clothing and accessories"));

        categoryRepository.saveAll(categories);
        log.info("Seeded {} categories.", categories.size());

        return categories.stream()
                .collect(Collectors.toMap(Category::getName, Function.identity()));
    }

    private Category buildCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    private Map<String, Category> loadCategoriesByName() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getName, Function.identity()));
    }

    // -------------------------------------------------------------------------
    // Products
    // -------------------------------------------------------------------------

    private void seedProducts(Map<String, Category> categories) {
        if (productRepository.count() > 0) {
            log.debug("Products already seeded, skipping.");
            return;
        }

        if (categories.isEmpty()) {
            log.warn("No categories available — skipping product seeding.");
            return;
        }

        List<Product> products = List.of(
                buildProduct("Smartphone X", "Latest generation smartphone with high-resolution display", "799.99", 50,
                        4.5, categories.get("Electronics")),
                buildProduct("Ultrabook Pro", "Lightweight laptop with long battery life", "1299.00", 30, 4.7,
                        categories.get("Electronics")),
                buildProduct("Mystery Novel", "Bestselling mystery thriller book", "19.99", 100, 4.2,
                        categories.get("Books")),
                buildProduct("Basic T-Shirt", "Comfortable cotton t-shirt", "9.99", 200, 4.0,
                        categories.get("Fashion")));

        productRepository.saveAll(products);
        log.info("Seeded {} products.", products.size());
    }

    private Product buildProduct(String name, String description,
            String price, int stock,
            double rating, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setRating(rating);
        product.setCategory(category);
        return product;
    }
}