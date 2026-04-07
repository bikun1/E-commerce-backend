package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.request.ProductFilterDTO;
import com.example.ecommerce.backend.dto.request.ProductRequest;
import com.example.ecommerce.backend.dto.request.UpdateProductRequest;
import com.example.ecommerce.backend.dto.response.PagedResponse;
import com.example.ecommerce.backend.dto.response.ProductResponse;
import com.example.ecommerce.backend.entity.Product;

import org.springframework.data.jpa.domain.Specification;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, UpdateProductRequest request);

    void delete(Long id);

    void restore(Long id);

    ProductResponse getById(Long id);

    PagedResponse<ProductResponse> getAllProducts(ProductFilterDTO filter, int page, int size, String sortBy,
            String sortDir);

    Specification<Product> buildSpecification(ProductFilterDTO filter);
}
