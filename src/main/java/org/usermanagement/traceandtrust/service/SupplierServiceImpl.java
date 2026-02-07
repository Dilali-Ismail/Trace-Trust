package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateSupplierRequest;
import org.usermanagement.traceandtrust.dto.SupplierDto;
import org.usermanagement.traceandtrust.dto.UpdateSupplierRequest;
import org.usermanagement.traceandtrust.entity.Supplier;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.SupplierMapper;
import org.usermanagement.traceandtrust.repository.SupplierRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService{

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierDto createSupplier(CreateSupplierRequest request) {

        supplierRepository.findByName(request.getName()).ifPresent(s -> {
            throw new DuplicateResourceException("Supplier with name '" + request.getName() + "' already exists.");
        });

        Supplier newSupplier = supplierMapper.toEntity(request);
        Supplier savedSupplier = supplierRepository.save(newSupplier);

        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    public List<SupplierDto> getAllSuppliers() {

        return supplierRepository.findAll().stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierDto getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return supplierMapper.toDto(supplier);
    }

    @Override
    @Transactional
    public SupplierDto updateSupplier(UUID id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        // Check if name is being updated and if it conflicts with another supplier
        if (request.getName() != null && !request.getName().equals(supplier.getName())) {
            supplierRepository.findByName(request.getName()).ifPresent(s -> {
                throw new DuplicateResourceException("Supplier with name '" + request.getName() + "' already exists.");
            });
            supplier.setName(request.getName());
        }

        // Update contactInfo if provided
        if (request.getContactInfo() != null) {
            supplier.setContactInfo(request.getContactInfo());
        }

        // Update active status if provided
        if (request.getActive() != null) {
            supplier.setActive(request.getActive());
        }

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toDto(updatedSupplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        
        // Soft delete: set active to false instead of deleting from database
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }
}
