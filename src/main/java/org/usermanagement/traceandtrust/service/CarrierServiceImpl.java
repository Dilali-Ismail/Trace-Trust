package org.usermanagement.traceandtrust.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CarrierDto;
import org.usermanagement.traceandtrust.dto.CreateCarrierRequest;
import org.usermanagement.traceandtrust.entity.Carrier;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.CarrierMapper;
import org.usermanagement.traceandtrust.repository.CarrierRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarrierServiceImpl implements CarrierService {
    private final CarrierRepository carrierRepository;
    private final CarrierMapper carrierMapper;

    @Override
    public CarrierDto createCarrier(CreateCarrierRequest request) {
        Carrier carrier = carrierMapper.toEntity(request);
        return carrierMapper.toDto(carrierRepository.save(carrier));
    }
    @Override
    public List<CarrierDto> getAllCarriers() {
        return carrierRepository.findAll().stream().map(carrierMapper::toDto).collect(Collectors.toList());
    }
}
