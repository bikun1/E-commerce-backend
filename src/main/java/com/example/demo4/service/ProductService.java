package com.example.demo4.service;

import com.example.demo4.dto.request.ProductFilterDTO;
import com.example.demo4.dto.request.ProductRequest;
import com.example.demo4.dto.request.UpdateProductRequest;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.dto.response.ProductResponse;
import com.example.demo4.entity.Product;

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
