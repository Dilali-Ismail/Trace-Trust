package org.usermanagement.traceandtrust.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Product")
public class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    // ============================================
    // DONNÉES DE TEST
    // ============================================

    private UUID adminId;
    private UUID userId;
    private UUID productId;
    private User adminUser;
    private User regularUser;
    private Product product;
    private ProductDto productDto;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        // IDs
        adminId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Utilisateur ADMIN
        adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@tracetrust.com");
        adminUser.setRole(Role.ADMIN);

        // Utilisateur régulier
        regularUser = new User();
        regularUser.setId(userId);
        regularUser.setName("Regular User");
        regularUser.setEmail("user@example.com");
        regularUser.setRole(Role.USER);

        // Produit
        product = new Product();
        product.setId(productId);
        product.setSku("PROD-001");
        product.setName("Test Product");
        product.setCategory("Electronics");
        product.setCostPrice(BigDecimal.valueOf(99.99));
        product.setActive(true);

        // ProductDto
        productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setSku("PROD-001");
        productDto.setName("Test Product");
        productDto.setCategory("Electronics");
        productDto.setCostPrice(BigDecimal.valueOf(99.99));
        productDto.setActive(true);

        // Requête de création
        createRequest = new CreateProductRequest();
        createRequest.setSku("PROD-001");
        createRequest.setName("Test Product");
        createRequest.setCategory("Electronics");
        createRequest.setCostPrice(BigDecimal.valueOf(99.99));

        // Requête de mise à jour
        updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product Name");
        updateRequest.setCategory("Updated Category");
        updateRequest.setCostPrice(BigDecimal.valueOf(149.99));
        updateRequest.setActive(true);
    }

    // ============================================
    // TESTS - CREATE PRODUCT
    // ============================================

    @Test
    @DisplayName("Créer un produit - Succès (n'importe quel utilisateur authentifié)")
    void createProduct_Success_WithAuthenticatedUser() {
        // ========== ARRANGE (Given) ==========
        // N'importe quel utilisateur peut créer un produit (pas de vérification ADMIN)
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));
        when(productRepository.findBySku("PROD-001")).thenReturn(Optional.empty());
        when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        // ========== ACT (When) ==========
        ProductDto result = productService.createProduct(createRequest);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getSku()).isEqualTo("PROD-001");
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getCostPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        assertThat(result.isActive()).isTrue();

        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, times(1)).findBySku("PROD-001");
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toEntity(createRequest);
        verify(productMapper, times(1)).toDto(product);
    }

    @Test
    @DisplayName("Créer un produit - Échec : SKU déjà existant")
    void createProduct_ThrowsDuplicateResourceException_WhenSkuExists() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));
        when(productRepository.findBySku("PROD-001")).thenReturn(Optional.of(product));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product with SKU 'PROD-001' already exists");

        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, times(1)).findBySku("PROD-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Créer un produit - Échec : Utilisateur non trouvé")
    void createProduct_ThrowsResourceNotFoundException_WhenUserNotFound() {
        // ========== ARRANGE (Given) ==========
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actor user with ID")
                .hasMessageContaining(unknownId.toString());

        verify(userRepository, times(1)).findById(unknownId);
        verify(productRepository, never()).save(any(Product.class));
    }

    // ============================================
    // TESTS - GET ALL PRODUCTS
    // ============================================

    @Test
    @DisplayName("Récupérer tous les produits - Succès avec ADMIN")
    void getAllProducts_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        UUID product2Id = UUID.randomUUID();
        Product product2 = new Product();
        product2.setId(product2Id);
        product2.setSku("PROD-002");
        product2.setName("Second Product");
        product2.setCategory("Books");
        product2.setCostPrice(BigDecimal.valueOf(29.99));
        product2.setActive(true);

        ProductDto productDto2 = new ProductDto();
        productDto2.setId(product2Id);
        productDto2.setSku("PROD-002");
        productDto2.setName("Second Product");
        productDto2.setCategory("Books");
        productDto2.setCostPrice(BigDecimal.valueOf(29.99));
        productDto2.setActive(true);

        List<Product> products = Arrays.asList(product, product2);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.findAllByActiveTrue()).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(productDto);
        when(productMapper.toDto(product2)).thenReturn(productDto2);

        // ========== ACT (When) ==========
        List<ProductDto> results = productService.getAllProducts();

        // ========== ASSERT (Then) ==========
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getSku()).isEqualTo("PROD-001");
        assertThat(results.get(1).getSku()).isEqualTo("PROD-002");

        verify(userRepository, times(1)).findById(adminId);
        verify(productRepository, times(1)).findAllByActiveTrue();
        verify(productMapper, times(2)).toDto(any(Product.class));
    }

    @Test
    @DisplayName("Récupérer tous les produits - Échec : Utilisateur non ADMIN")
    void getAllProducts_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.getAllProducts())
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, never()).findAllByActiveTrue();
    }

    // ============================================
    // TESTS - GET PRODUCT BY ID
    // ============================================

    @Test
    @DisplayName("Récupérer un produit par ID - Succès avec ADMIN")
    void getProductById_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto);

        // ========== ACT (When) ==========
        ProductDto result = productService.getProductById(productId);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getSku()).isEqualTo("PROD-001");
        assertThat(result.getName()).isEqualTo("Test Product");

        verify(userRepository, times(1)).findById(adminId);
        verify(productRepository, times(1)).findByIdAndActiveTrue(productId);
        verify(productMapper, times(1)).toDto(product);
    }

    @Test
    @DisplayName("Récupérer un produit par ID - Échec : Produit non trouvé ou inactif")
    void getProductById_ThrowsResourceNotFoundException_WhenNotFoundOrInactive() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product with ID")
                .hasMessageContaining(productId.toString());

        verify(userRepository, times(1)).findById(adminId);
        verify(productRepository, times(1)).findByIdAndActiveTrue(productId);
    }

    @Test
    @DisplayName("Récupérer un produit par ID - Échec : Utilisateur non ADMIN")
    void getProductById_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, never()).findByIdAndActiveTrue(any());
    }

    // ============================================
    // TESTS - UPDATE PRODUCT
    // ============================================

    @Test
    @DisplayName("Mettre à jour un produit - Succès avec ADMIN")
    void updateProduct_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setSku("PROD-001");
        updatedProduct.setName("Updated Product Name");
        updatedProduct.setCategory("Updated Category");
        updatedProduct.setCostPrice(BigDecimal.valueOf(149.99));
        updatedProduct.setActive(true);

        ProductDto updatedDto = new ProductDto();
        updatedDto.setId(productId);
        updatedDto.setSku("PROD-001");
        updatedDto.setName("Updated Product Name");
        updatedDto.setCategory("Updated Category");
        updatedDto.setCostPrice(BigDecimal.valueOf(149.99));
        updatedDto.setActive(true);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(updatedDto);

        // ========== ACT (When) ==========
        ProductDto result = productService.updateProduct(productId, updateRequest);

        // ========== ASSERT (Then) ==========
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Updated Product Name");
        assertThat(result.getCategory()).isEqualTo("Updated Category");
        assertThat(result.getCostPrice()).isEqualByComparingTo(BigDecimal.valueOf(149.99));

        verify(userRepository, times(1)).findById(adminId);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);
        verify(productMapper, times(1)).toDto(updatedProduct);
    }

    @Test
    @DisplayName("Mettre à jour un produit - Échec : Produit non trouvé")
    void updateProduct_ThrowsResourceNotFoundException_WhenProductNotFound() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.updateProduct(productId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product with ID")
                .hasMessageContaining(productId.toString());

        verify(userRepository, times(1)).findById(adminId);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Mettre à jour un produit - Échec : Utilisateur non ADMIN")
    void updateProduct_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.updateProduct(productId, updateRequest))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
    }

    // ============================================
    // TESTS - DELETE PRODUCT (Soft Delete)
    // ============================================

    @Test
    @DisplayName("Supprimer un produit (soft delete) - Succès avec ADMIN")
    void deleteProduct_Success_WithAdminUser() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // ========== ACT (When) ==========
        productService.deleteProduct(productId);

        // ========== ASSERT (Then) ==========
        verify(userRepository, times(1)).findById(adminId);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);

        // Vérifier que le produit a été désactivé
        assertThat(product.isActive()).isFalse();
    }

    @Test
    @DisplayName("Supprimer un produit - Échec : Produit non trouvé")
    void deleteProduct_ThrowsResourceNotFoundException_WhenProductNotFound() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product with ID")
                .hasMessageContaining(productId.toString());

        verify(userRepository, times(1)).findById(adminId);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Supprimer un produit - Échec : Utilisateur non ADMIN")
    void deleteProduct_ThrowsForbiddenAccessException_WhenUserIsNotAdmin() {
        // ========== ARRANGE (Given) ==========
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // ========== ACT & ASSERT (When & Then) ==========
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
    }

}
