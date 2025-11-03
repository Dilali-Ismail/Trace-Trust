package org.usermanagement.traceandtrust.service;

import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request, UUID actorId);
    List<ProductDto> getAllProducts(UUID actorId);
    ProductDto getProductById(UUID productId, UUID actorId);
}
