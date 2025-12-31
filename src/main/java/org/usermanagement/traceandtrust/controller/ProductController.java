package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;
import org.usermanagement.traceandtrust.dto.UpdateProductRequest;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.service.ProductService;
import org.usermanagement.traceandtrust.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserService userService; // 1. Injection du UserService

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(
            @AuthenticationPrincipal Jwt jwt, // 2. On récupère le Token Keycloak
            @Valid @RequestBody CreateProductRequest request) {

        // 3. On synchronise/récupère l'utilisateur local
        User user = userService.syncUser(jwt);

        // 4. On passe l'ID local au service
        ProductDto createdProduct = productService.createProduct(request, user.getId());
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @AuthenticationPrincipal Jwt jwt) { // Plus besoin de Header manuel

        User user = userService.syncUser(jwt);
        List<ProductDto> products = productService.getAllProducts(user.getId());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable UUID productId,
            @AuthenticationPrincipal Jwt jwt) {

        User user = userService.syncUser(jwt);
        ProductDto product = productService.getProductById(productId, user.getId());
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProductRequest request) {

        User user = userService.syncUser(jwt);
        ProductDto updatedProduct = productService.updateProduct(productId, request, user.getId());
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal Jwt jwt) {

        User user = userService.syncUser(jwt);
        productService.deleteProduct(productId, user.getId());

        return ResponseEntity.noContent().build();
    }
}