package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.request.CategoryFilterDTO;
import com.example.ecommerce.backend.dto.request.CategoryRequest;
import com.example.ecommerce.backend.dto.request.UpdateCategoryRequest;
import com.example.ecommerce.backend.dto.response.CategoryResponse;
import com.example.ecommerce.backend.dto.response.PagedResponse;
import com.example.ecommerce.backend.dto.response.PaginationResponse;
import com.example.ecommerce.backend.entity.Category;
import com.example.ecommerce.backend.exception.ConflictException;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.CategoryRepository;
import com.example.ecommerce.backend.service.CategoryService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.countByNameIncludeDeleted(request.name()) > 0) {
            throw new ConflictException("Category name already exists");
        }
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest updateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (updateRequest.name() != null) {
            if (categoryRepository.countByNameIncludeDeleted(updateRequest.name()) > 0
                    && !category.getName().equals(updateRequest.name())) {
                throw new ConflictException("Category name already exists");
            }
            category.setName(updateRequest.name());
        }

        if (updateRequest.description() != null) {
            category.setDescription(updateRequest.description());
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (categoryRepository.softDeleteById(id) == 0) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

    }

    @Override
    @Transactional
    public void restore(Long id) {
        if (categoryRepository.restoreById(id) == 0) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getAllCategories(
            CategoryFilterDTO filter, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoryResponse> categoryPage = categoryRepository
                .findAll(buildSpecification(filter), pageable)
                .map(CategoryResponse::fromEntity);

        PaginationResponse pagination = new PaginationResponse(
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalPages(),
                categoryPage.getTotalElements());

        return new PagedResponse<>(categoryPage.getContent(), pagination);
    }

    @Override
    public Specification<Category> buildSpecification(CategoryFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + filter.name().toLowerCase() + "%"));
            }

            if (filter.description() != null && !filter.description().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("description")),
                                "%" + filter.description().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}


