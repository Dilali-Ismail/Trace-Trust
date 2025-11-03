package org.usermanagement.traceandtrust.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;
import org.usermanagement.traceandtrust.dto.UpdateProductRequest;
import org.usermanagement.traceandtrust.entity.Product;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.ProductMapper;
import org.usermanagement.traceandtrust.repository.ProductRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    public ProductDto createProduct(CreateProductRequest request, UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user with ID " + actorId + " not found."));
        productRepository.findBySku(request.getSku()).ifPresent(product -> {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists.");
        });
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    public List<ProductDto> getAllProducts(UUID actorId) {
        checkAdminRole(actorId);
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(UUID productId, UUID actorId) {

        checkAdminRole(actorId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
        return productMapper.toDto(product);


    }

    public ProductDto updateProduct(UUID productId, UpdateProductRequest request, UUID actorId){
        checkAdminRole(actorId);
        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
        productToUpdate.setName(request.getName());
        productToUpdate.setCategory(request.getCategory());
        productToUpdate.setCostPrice(request.getCostPrice());
        productToUpdate.setActive(request.getActive());

        Product updatedProduct = productRepository.save(productToUpdate);

        return productMapper.toDto(updatedProduct);

    }

    private void checkAdminRole(UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user with ID " + actorId + " not found."));

        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("This action can only be performed by an ADMIN user.");
        }
    }


}
