package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.request.ProductFilterDTO;
import com.example.ecommerce.backend.dto.request.ProductRequest;
import com.example.ecommerce.backend.dto.request.UpdateProductRequest;
import com.example.ecommerce.backend.dto.response.PagedResponse;
import com.example.ecommerce.backend.dto.response.PaginationResponse;
import com.example.ecommerce.backend.dto.response.ProductResponse;
import com.example.ecommerce.backend.entity.Category;
import com.example.ecommerce.backend.entity.Product;
import com.example.ecommerce.backend.exception.BadRequestException;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.CategoryRepository;
import com.example.ecommerce.backend.repository.ProductRepository;
import com.example.ecommerce.backend.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        // @NotNull/@NotBlank guarantees name, price, stock are non-null here
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());

        if (request.categoryId() != null) {
            product.setCategory(fetchCategory(request.categoryId()));
        }

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new BadRequestException("Product name must not be blank");
            }
            product.setName(request.name());
        }
        if (request.description() != null)
            product.setDescription(request.description());
        if (request.price() != null)
            product.setPrice(request.price());
        if (request.stock() != null)
            product.setStock(request.stock());
        if (request.categoryId() != null)
            product.setCategory(fetchCategory(request.categoryId()));

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void delete(Long id) {
        // Single query — avoids the existsById + softDelete double-hit
        if (productRepository.softDeleteById(id) == 0) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void restore(Long id) {
        if (productRepository.restoreById(id) == 0) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "products", key = "#root.methodName + #page + #size + #sortBy + #sortDir + #filter")
    public PagedResponse<ProductResponse> getAllProducts(
            ProductFilterDTO filter, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> productPage = productRepository
                .findAll(buildSpecification(filter), pageable)
                .map(ProductResponse::fromEntity);

        PaginationResponse pagination = new PaginationResponse(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages(),
                productPage.getTotalElements());

        return new PagedResponse<>(productPage.getContent(), pagination);
    }

    @Override
    public Specification<Product> buildSpecification(ProductFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.name().toLowerCase() + "%"));
            }
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (filter.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.minRating()));
            }
            if (filter.minStock() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("stock"), filter.minStock()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    // ---------------------------------------------------------------- helpers

    private Category fetchCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }
}


