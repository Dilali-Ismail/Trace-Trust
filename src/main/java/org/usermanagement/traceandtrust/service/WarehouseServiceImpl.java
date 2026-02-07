package org.usermanagement.traceandtrust.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.UpdateWarehouseRequest;
import org.usermanagement.traceandtrust.dto.WarehouseDto;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.entity.Warehouse;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.DuplicateResourceException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.WarehouseMapper;
import org.usermanagement.traceandtrust.repository.UserRepository;
import org.usermanagement.traceandtrust.repository.WarehouseRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService{
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseDto createWarehouse(CreateWarehouseRequest request) {

        warehouseRepository.findByCode(request.getCode()).ifPresent(w -> {
            throw new DuplicateResourceException("Warehouse with code '" + request.getCode() + "' already exists.");
        });

        Warehouse warehouse = warehouseMapper.toEntity(request);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toDto(savedWarehouse);
    }
    public List<WarehouseDto> getAllWarehouses() {
        return warehouseRepository.findAllByActiveTrue()
                .stream()
                .map(warehouseMapper::toDto)
                .collect(Collectors.toList());
    }
    public WarehouseDto getWarehouseById(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findByIdAndActiveTrue(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Active warehouse with ID " + warehouseId + " not found."));
        return warehouseMapper.toDto(warehouse);
    }
    public WarehouseDto updateWarehouse(UUID warehouseId, UpdateWarehouseRequest request) {

        Warehouse warehouseToUpdate = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse with ID " + warehouseId + " not found."));

        // Use the mapper to update the entity from the DTO
        warehouseMapper.updateFromDto(request, warehouseToUpdate);

        Warehouse updatedWarehouse = warehouseRepository.save(warehouseToUpdate);
        return warehouseMapper.toDto(updatedWarehouse);
    }
    public void deleteWarehouse(UUID warehouseId) {

        Warehouse warehouseToDelete = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse with ID " + warehouseId + " not found."));

        warehouseToDelete.setActive(false);
        warehouseRepository.save(warehouseToDelete);
    }

    }
