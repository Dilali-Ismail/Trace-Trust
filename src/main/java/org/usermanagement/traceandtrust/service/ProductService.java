package org.usermanagement.traceandtrust.service;

import org.springframework.web.multipart.MultipartFile;
import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;
import org.usermanagement.traceandtrust.dto.UpdateProductRequest;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request, MultipartFile image);
    List<ProductDto> getAllProducts();
    ProductDto getProductById(UUID productId);
    ProductDto getProductBySku(String sku);
    List<ProductDto> getProductsByCategory(String category);
    List<String> getAllCategories();
    ProductDto updateProduct(UUID productId, UpdateProductRequest request, MultipartFile image);
    void deleteProduct(UUID productId);
}
