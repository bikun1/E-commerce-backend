package com.example.ecommerce.backend.service;

import org.springframework.data.jpa.domain.Specification;

import com.example.ecommerce.backend.dto.request.CategoryFilterDTO;
import com.example.ecommerce.backend.dto.request.CategoryRequest;
import com.example.ecommerce.backend.dto.request.UpdateCategoryRequest;
import com.example.ecommerce.backend.dto.response.CategoryResponse;
import com.example.ecommerce.backend.dto.response.PagedResponse;
import com.example.ecommerce.backend.entity.Category;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, UpdateCategoryRequest updateRequest);

    void delete(Long id);

    void restore(Long id);

    CategoryResponse getById(Long id);

    PagedResponse<CategoryResponse> getAllCategories(CategoryFilterDTO filter, int page, int size, String sortBy,
            String sortDir);

    Specification<Category> buildSpecification(CategoryFilterDTO filter);
}
