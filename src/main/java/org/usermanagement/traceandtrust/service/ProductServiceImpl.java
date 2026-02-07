package org.usermanagement.traceandtrust.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;
import org.usermanagement.traceandtrust.dto.UpdateProductRequest;
import org.usermanagement.traceandtrust.entity.Product;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.ProductMapper;
import org.usermanagement.traceandtrust.repository.ProductRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final S3Service s3Service;

    @Override
    public ProductDto createProduct(CreateProductRequest request, MultipartFile image) {
        productRepository.findBySku(request.getSku()).ifPresent(product -> {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists.");
        });

        Product product = productMapper.toEntity(request);
        
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image);
            product.setImageUrl(imageUrl);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAllByActiveTrue()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySkuAndActiveTrue(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product with SKU '" + sku + "' not found or is inactive."));
        return productMapper.toDto(product);
    }

    @Override
    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        return productRepository.findAllDistinctCategories();
    }

    @Override
    public ProductDto updateProduct(UUID productId, UpdateProductRequest request, MultipartFile image) {
        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
        
        productToUpdate.setName(request.getName());
        productToUpdate.setCategory(request.getCategory());
        productToUpdate.setCostPrice(request.getCostPrice());
        productToUpdate.setActive(request.getActive());

        if (image != null && !image.isEmpty()) {
            // Delete old image if it exists
            if (productToUpdate.getImageUrl() != null && !productToUpdate.getImageUrl().isEmpty()) {
                s3Service.deleteFile(productToUpdate.getImageUrl());
            }
            // Upload new image
            String imageUrl = s3Service.uploadFile(image);
            productToUpdate.setImageUrl(imageUrl);
        }

        Product updatedProduct = productRepository.save(productToUpdate);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    public void deleteProduct(UUID productId) {
        Product productToDelete = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
        
        // Logical delete (as per current implementation)
        productToDelete.setActive(false);
        
        // Optional: If we wanted to delete the image on physical deletion, we would call s3Service.deleteFile
        // For now, following the existing logical delete pattern.
        
        productRepository.save(productToDelete);
    }
}
