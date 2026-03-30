package com.example.demo4.service;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo4.dto.request.CategoryFilterDTO;
import com.example.demo4.dto.request.CategoryRequest;
import com.example.demo4.dto.request.UpdateCategoryRequest;
import com.example.demo4.dto.response.CategoryResponse;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.entity.Category;

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
