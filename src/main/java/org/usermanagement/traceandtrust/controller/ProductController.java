package org.usermanagement.traceandtrust.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.usermanagement.traceandtrust.dto.CreateProductRequest;
import org.usermanagement.traceandtrust.dto.ProductDto;
import org.usermanagement.traceandtrust.dto.UpdateProductRequest;
import org.usermanagement.traceandtrust.service.ProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ProductDto> createProduct(
            @RequestHeader("X-Actor-ID") UUID actorId,
            @Valid @RequestBody CreateProductRequest request){

        ProductDto createdProduct = productService.createProduct(request, actorId);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestHeader("X-Actor-ID") UUID actorId) {

        List<ProductDto> products = productService.getAllProducts(actorId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable UUID productId,
            @RequestHeader("X-Actor-ID") UUID actorId) {

        ProductDto product = productService.getProductById(productId, actorId);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID productId,
            @RequestHeader("X-Actor-ID") UUID actorId,
            @Valid @RequestBody UpdateProductRequest request) {

        ProductDto updatedProduct = productService.updateProduct(productId, request, actorId);
        return ResponseEntity.ok(updatedProduct);
    }
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID productId,
            @RequestHeader("X-Actor-ID") UUID actorId) {

        productService.deleteProduct(productId, actorId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{sku}/deactivate")
    public ResponseEntity<ProductDto> deactivateProduct(
            @PathVariable  String sku,
            @RequestHeader("X-Actor-ID") UUID actorId) {

        ProductDto deactivatedProduct = productService.deactivateProduct(sku, actorId);
        return ResponseEntity.ok(deactivatedProduct);
    }
}
