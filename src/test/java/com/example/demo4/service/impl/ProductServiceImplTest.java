package com.example.demo4.service.impl;

import com.example.demo4.dto.request.ProductFilterDTO;
import com.example.demo4.dto.request.ProductRequest;
import com.example.demo4.dto.request.UpdateProductRequest;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.dto.response.ProductResponse;
import com.example.demo4.entity.Category;
import com.example.demo4.entity.Product;
import com.example.demo4.exception.BadRequestException;
import com.example.demo4.exception.ResourceNotFoundException;
import com.example.demo4.repository.CategoryRepository;
import com.example.demo4.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

    // ── constants ────────────────────────────────────────────────────────────
    private static final long PRODUCT_ID = 1L;
    private static final long CATEGORY_ID = 1L;
    private static final long MISSING_ID = 999L;
    private static final String PRODUCT_NAME = "Laptop";
    private static final String DESCRIPTION = "Gaming laptop";
    private static final BigDecimal PRICE = new BigDecimal("999.99");
    private static final int STOCK = 50;

    // ── mocks ────────────────────────────────────────────────────────────────
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    // ── fixture builders ─────────────────────────────────────────────────────

    private static Product product(long id, String name, BigDecimal price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        return p;
    }

    private static Product defaultProduct() {
        return product(PRODUCT_ID, PRODUCT_NAME, PRICE, STOCK);
    }

    private static Category category(long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private static Category defaultCategory() {
        return category(CATEGORY_ID, "Electronics");
    }

    private static ProductRequest createRequest() {
        return ProductRequest.empty()
                .withName(PRODUCT_NAME)
                .withDescription(DESCRIPTION)
                .withPrice(PRICE)
                .withStock(STOCK)
                .withCategoryId(CATEGORY_ID);
    }

    private static UpdateProductRequest updateRequest(String name, String desc,
            BigDecimal price, Integer stock) {
        return UpdateProductRequest.empty()
                .withName(name)
                .withDescription(desc)
                .withPrice(price)
                .withStock(stock);
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("saves product and returns ProductResponse on success")
        void savesAndReturnsResponse() {
            Category cat = defaultCategory();
            Product saved = defaultProduct();
            saved.setCategory(cat);

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(cat));
            when(productRepository.save(any(Product.class))).thenReturn(saved);

            ProductResponse result = productService.create(createRequest());

            assertThat(result.name()).isEqualTo(PRODUCT_NAME);
            assertThat(result.id()).isEqualTo(PRODUCT_ID);
            verify(productRepository).save(argThat(p -> p.getName().equals(PRODUCT_NAME) && p.getStock() == STOCK));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category does not exist")
        void throwsWhenCategoryNotFound() {
            ProductRequest request = ProductRequest.empty()
                    .withName(PRODUCT_NAME)
                    .withPrice(PRICE)
                    .withStock(STOCK)
                    .withCategoryId(MISSING_ID);

            when(categoryRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));

            verify(productRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates all fields and returns ProductResponse on success")
        void updatesAllFields() {
            Product existing = product(PRODUCT_ID, "Old Name", BigDecimal.TEN, 10);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existing));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductResponse result = productService.update(PRODUCT_ID,
                    updateRequest("New Name", "New desc", new BigDecimal("20.00"), 20));

            assertThat(result.name()).isEqualTo("New Name");
            assertThat(existing.getName()).isEqualTo("New Name");
            assertThat(existing.getStock()).isEqualTo(20);
            assertThat(existing.getPrice()).isEqualByComparingTo("20.00");
            assertThat(existing.getDescription()).isEqualTo("New desc");
        }

        @Test
        @DisplayName("updates category when categoryId is provided")
        void updatesCategory() {
            Product existing = product(PRODUCT_ID, PRODUCT_NAME, PRICE, STOCK);
            Category newCategory = category(2L, "Phones");

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existing));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            productService.update(PRODUCT_ID,
                    UpdateProductRequest.empty().withCategoryId(2L));

            assertThat(existing.getCategory()).isEqualTo(newCategory);
        }

        @Test
        @DisplayName("throws BadRequestException when name is blank")
        void throwsWhenNameBlank() {
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.of(defaultProduct()));

            assertThatThrownBy(() -> productService.update(PRODUCT_ID,
                    UpdateProductRequest.empty().withName("   ")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Product name must not be blank");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void throwsWhenProductNotFound() {
            when(productRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(MISSING_ID,
                    UpdateProductRequest.empty().withName("Name")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when new category does not exist")
        void throwsWhenCategoryNotFound() {
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.of(defaultProduct()));
            when(categoryRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(PRODUCT_ID,
                    UpdateProductRequest.empty().withCategoryId(MISSING_ID)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));

            verify(productRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("calls softDeleteById when product exists")
        void softDeletesSuccessfully() {
            when(productRepository.softDeleteById(PRODUCT_ID)).thenReturn(1);

            productService.delete(PRODUCT_ID);

            verify(productRepository).softDeleteById(PRODUCT_ID);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when softDeleteById affects 0 rows")
        void throwsWhenProductNotFound() {
            // softDeleteById returns 0 → product did not exist
            when(productRepository.softDeleteById(MISSING_ID)).thenReturn(0);

            assertThatThrownBy(() -> productService.delete(MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));

            verify(productRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("restore()")
    class Restore {

        @Test
        @DisplayName("calls restoreById when product exists")
        void restoresSuccessfully() {
            when(productRepository.restoreById(PRODUCT_ID)).thenReturn(1);

            productService.restore(PRODUCT_ID);

            verify(productRepository).restoreById(PRODUCT_ID);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when restoreById affects 0 rows")
        void throwsWhenProductNotFound() {
            when(productRepository.restoreById(MISSING_ID)).thenReturn(0);

            assertThatThrownBy(() -> productService.restore(MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns ProductResponse when product exists")
        void returnsProduct() {
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.of(defaultProduct()));

            ProductResponse result = productService.getById(PRODUCT_ID);

            assertThat(result.id()).isEqualTo(PRODUCT_ID);
            assertThat(result.name()).isEqualTo(PRODUCT_NAME);
            assertThat(result.price()).isEqualByComparingTo(PRICE);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void throwsWhenNotFound() {
            when(productRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getById(MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProducts {

        @Test
        @DisplayName("returns paged content with correct metadata")
        void returnsPagedResponse() {
            Product p = product(PRODUCT_ID, "P1", BigDecimal.ONE, 5);
            Page<Product> page = new PageImpl<>(
                    List.of(p),
                    PageRequest.of(0, 10),
                    1);

            when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(page);

            PagedResponse<ProductResponse> result = productService.getAllProducts(
                    ProductFilterDTO.empty(), 0, 10, "id", "asc");

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("P1");
            assertThat(result.pagination().page()).isZero();
            assertThat(result.pagination().pageSize()).isEqualTo(10);
            assertThat(result.pagination().totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty content when no products match filter")
        void returnsEmptyWhenNoMatch() {
            Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(emptyPage);

            PagedResponse<ProductResponse> result = productService.getAllProducts(
                    ProductFilterDTO.empty(), 0, 10, "id", "asc");

            assertThat(result.content()).isEmpty();
            assertThat(result.pagination().totalElements()).isZero();
        }
    }
}