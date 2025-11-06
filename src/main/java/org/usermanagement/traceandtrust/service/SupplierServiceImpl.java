package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateSupplierRequest;
import org.usermanagement.traceandtrust.dto.SupplierDto;
import org.usermanagement.traceandtrust.entity.Supplier;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.SupplierMapper;
import org.usermanagement.traceandtrust.repository.SupplierRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService{

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierDto createSupplier(CreateSupplierRequest request, UUID actorId) {
        checkAdminRole(actorId);

        supplierRepository.findByName(request.getName()).ifPresent(s -> {
            throw new DuplicateResourceException("Supplier with name '" + request.getName() + "' already exists.");
        });

        Supplier newSupplier = supplierMapper.toEntity(request);
        Supplier savedSupplier = supplierRepository.save(newSupplier);

        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    public List<SupplierDto> getAllSuppliers(UUID actorId) {
        checkAdminRole(actorId);

        return supplierRepository.findAll().stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    private void checkAdminRole(UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + actorId));
        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("This operation is restricted to ADMIN users.");
        }
    }
}
