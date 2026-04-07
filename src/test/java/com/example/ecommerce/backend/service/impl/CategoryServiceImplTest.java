package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.request.CategoryFilterDTO;
import com.example.ecommerce.backend.dto.request.CategoryRequest;
import com.example.ecommerce.backend.dto.request.UpdateCategoryRequest;
import com.example.ecommerce.backend.dto.response.CategoryResponse;
import com.example.ecommerce.backend.dto.response.PagedResponse;
import com.example.ecommerce.backend.entity.Category;
import com.example.ecommerce.backend.exception.ConflictException;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.CategoryRepository;
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
@DisplayName("CategoryServiceImpl")
class CategoryServiceImplTest {

    // ── constants ────────────────────────────────────────────────────────────
    private static final long CATEGORY_ID = 1L;
    private static final long MISSING_ID = 999L;
    private static final String NAME = "Electronics";
    private static final String DESCRIPTION = "Tech category";
    private static final String NEW_NAME = "Books";
    private static final String NEW_DESC = "New description";

    // ── mocks ────────────────────────────────────────────────────────────────
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // ── fixture builders ─────────────────────────────────────────────────────

    private static Category category(long id, String name, String description) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setDescription(description);
        return c;
    }

    private static Category defaultCategory() {
        return category(CATEGORY_ID, NAME, DESCRIPTION);
    }

    private static CategoryRequest createRequest(String name, String description) {
        return CategoryRequest.empty().withName(name).withDescription(description);
    }

    private static UpdateCategoryRequest updateRequest(String name, String description) {
        return UpdateCategoryRequest.empty().withName(name).withDescription(description);
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("saves and returns CategoryResponse when name is unique")
        void savesAndReturnsResponse() {
            Category saved = defaultCategory();

            when(categoryRepository.countByNameIncludeDeleted(NAME)).thenReturn(0);
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);

            CategoryResponse result = categoryService.create(createRequest(NAME, DESCRIPTION));

            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.id()).isEqualTo(CATEGORY_ID);
            verify(categoryRepository).save(argThat(c -> c.getName().equals(NAME)));
        }

        @Test
        @DisplayName("throws ConflictException when name is already taken")
        void throwsWhenNameExists() {
            when(categoryRepository.countByNameIncludeDeleted(NAME)).thenReturn(1);

            assertThatThrownBy(() -> categoryService.create(createRequest(NAME, DESCRIPTION)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Category name already exists");

            verify(categoryRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates name and description when new name is unique")
        void updatesSuccessfully() {
            Category existing = defaultCategory();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));
            when(categoryRepository.countByNameIncludeDeleted(NEW_NAME)).thenReturn(0);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

            CategoryResponse result = categoryService.update(CATEGORY_ID, updateRequest(NEW_NAME, NEW_DESC));

            assertThat(result.name()).isEqualTo(NEW_NAME);
            assertThat(existing.getName()).isEqualTo(NEW_NAME);
            assertThat(existing.getDescription()).isEqualTo(NEW_DESC);
        }

        @Test
        @DisplayName("allows update when name is unchanged (same category)")
        void allowsSameNameUpdate() {
            Category existing = defaultCategory();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));
            // countByName returns 1 (itself), but name equals current — no conflict
            when(categoryRepository.countByNameIncludeDeleted(NAME)).thenReturn(1);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

            CategoryResponse result = categoryService.update(CATEGORY_ID, updateRequest(NAME, NEW_DESC));

            assertThat(result.name()).isEqualTo(NAME);
            assertThat(existing.getDescription()).isEqualTo(NEW_DESC);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category does not exist")
        void throwsWhenCategoryNotFound() {
            when(categoryRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(MISSING_ID, updateRequest(NEW_NAME, NEW_DESC)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }

        @Test
        @DisplayName("throws ConflictException when new name is already used by another category")
        void throwsWhenNewNameConflicts() {
            Category existing = defaultCategory();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));
            when(categoryRepository.countByNameIncludeDeleted(NEW_NAME)).thenReturn(1);

            assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, updateRequest(NEW_NAME, NEW_DESC)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Category name already exists");

            verify(categoryRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("calls softDeleteById when category exists")
        void softDeletesSuccessfully() {
            when(categoryRepository.softDeleteById(CATEGORY_ID)).thenReturn(1);

            categoryService.delete(CATEGORY_ID);

            verify(categoryRepository).softDeleteById(CATEGORY_ID);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when softDeleteById affects 0 rows")
        void throwsWhenCategoryNotFound() {
            // softDeleteById returns 0 → category did not exist
            when(categoryRepository.softDeleteById(MISSING_ID)).thenReturn(0);

            assertThatThrownBy(() -> categoryService.delete(MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));

            verify(categoryRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("restore()")
    class Restore {

        @Test
        @DisplayName("calls restoreById when category exists")
        void restoresSuccessfully() {
            when(categoryRepository.restoreById(CATEGORY_ID)).thenReturn(1);

            categoryService.restore(CATEGORY_ID);

            verify(categoryRepository).restoreById(CATEGORY_ID);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when restoreById affects 0 rows")
        void throwsWhenCategoryNotFound() {
            when(categoryRepository.restoreById(MISSING_ID)).thenReturn(0);

            assertThatThrownBy(() -> categoryService.restore(MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns CategoryResponse when category exists")
        void returnsCategory() {
            Category existing = defaultCategory();
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));

            CategoryResponse result = categoryService.getById(CATEGORY_ID);

            assertThat(result.id()).isEqualTo(CATEGORY_ID);
            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.description()).isEqualTo(DESCRIPTION);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category does not exist")
        void throwsWhenNotFound() {
            when(categoryRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getById(MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAllCategories()")
    class GetAllCategories {

        @Test
        @DisplayName("returns paged content with correct metadata")
        void returnsPagedResponse() {
            Category existing = defaultCategory();
            Page<Category> page = new PageImpl<>(
                    List.of(existing),
                    PageRequest.of(0, 10),
                    1);

            when(categoryRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(page);

            PagedResponse<CategoryResponse> result = categoryService.getAllCategories(
                    CategoryFilterDTO.empty(), 0, 10, "id", "asc");

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo(NAME);
            assertThat(result.pagination().page()).isZero();
            assertThat(result.pagination().pageSize()).isEqualTo(10);
            assertThat(result.pagination().totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty content when no categories match filter")
        void returnsEmptyWhenNoMatch() {
            Page<Category> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(categoryRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(emptyPage);

            PagedResponse<CategoryResponse> result = categoryService.getAllCategories(
                    CategoryFilterDTO.empty(), 0, 10, "id", "asc");

            assertThat(result.content()).isEmpty();
            assertThat(result.pagination().totalElements()).isZero();
        }
    }
}