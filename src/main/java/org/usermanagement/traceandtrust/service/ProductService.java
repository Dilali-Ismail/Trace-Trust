package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;
import org.usermanagement.traceandtrust.dto.UpdateProductRequest;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request);
    List<ProductDto> getAllProducts();
    ProductDto getProductById(UUID productId);
    ProductDto updateProduct(UUID productId, UpdateProductRequest request);
    void deleteProduct(UUID productId);
}
