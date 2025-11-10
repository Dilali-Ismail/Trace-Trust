package org.usermanagement.traceandtrust.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;
import org.usermanagement.traceandtrust.dto.UpdateProductRequest;
import org.usermanagement.traceandtrust.entity.Inventory;
import org.usermanagement.traceandtrust.entity.Product;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.ProductMapper;
import org.usermanagement.traceandtrust.repository.InventoryRepository;
import org.usermanagement.traceandtrust.repository.ProductRepository;
import org.usermanagement.traceandtrust.repository.SalesOrderLineRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository invontoryRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
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
        return productRepository.findAllByActiveTrue()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(UUID productId, UUID actorId) {

        checkAdminRole(actorId);
        Product product = productRepository.findByIdAndActiveTrue(productId)
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
    public void deleteProduct(UUID productId, UUID actorId){
        checkAdminRole(actorId);
        Product productToDelete = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
        productToDelete.setActive(false);
        productRepository.save(productToDelete);


    }
    //ajouter la methode pour desactiver le produits---------------------------------
    public ProductDto deactivateProduct(String sku, UUID actorId) {
        checkAdminRole(actorId);


        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        long activeOrderCount = salesOrderLineRepository.countByProductAndSalesOrder_StatusIn(
                product, Arrays.asList(SalesOrderStatus.CREATED, SalesOrderStatus.RESERVED));

        if (activeOrderCount > 0) {
            throw new BusinessException("Product can not be desactive he is belong to a reserved command ");
        }


        List<Inventory> inventories = invontoryRepository.findByProduct(product);
        long totalReservedStock = inventories.stream()
                .mapToLong(Inventory::getQuantity_reserved)
                .sum();

        if (totalReservedStock > 0) {
            throw new BusinessException("Porduct can not be desactive is already in a stck reserved ");
        }

        product.setActive(false);
        Product deactivatedProduct = productRepository.save(product);

        return productMapper.toDto(deactivatedProduct);
    }
    //------------------------------------------------------------------------------

    private void checkAdminRole(UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor user with ID " + actorId + " not found."));

        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("This action can only be performed by an ADMIN user.");
        }
    }


}
