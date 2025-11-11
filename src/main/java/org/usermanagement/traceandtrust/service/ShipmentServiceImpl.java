package org.usermanagement.traceandtrust.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.CreateShipmentRequest;
import org.usermanagement.traceandtrust.dto.ShipmentDto;
import org.usermanagement.traceandtrust.entity.Carrier;
import org.usermanagement.traceandtrust.entity.SalesOrder;
import org.usermanagement.traceandtrust.entity.Shipment;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.enums.SalesOrderStatus;
import org.usermanagement.traceandtrust.enums.ShipmentStatus;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.ShipmentMapper;
import org.usermanagement.traceandtrust.repository.CarrierRepository;
import org.usermanagement.traceandtrust.repository.SalesOrderRepository;
import org.usermanagement.traceandtrust.repository.ShipmentRepository;
import org.usermanagement.traceandtrust.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CarrierRepository carrierRepository;
    private final UserRepository userRepository;
    private final ShipmentMapper shipmentMapper;

   public  ShipmentDto createShipment(CreateShipmentRequest request, UUID actorId){

       checkWarehouseManagerOrAdminRole(actorId);

       SalesOrder salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
               .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with id: " + request.getSalesOrderId()));

       Carrier carrier = carrierRepository.findById(request.getCarrierId())
               .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + request.getCarrierId()));

       if (salesOrder.getStatus() != SalesOrderStatus.RESERVED) {
           throw new BusinessException("A shipment can only be created for an order with RESERVED status.");
       }

       if (!carrier.isActive()) {
           throw new BusinessException("Cannot create a shipment with an inactive carrier.");
       }

       if (shipmentRepository.existsBySalesOrderId(salesOrder.getId())) {
           throw new BusinessException("A shipment already exists for this sales order.");
       }

       Shipment shipment = new Shipment();
       shipment.setSalesOrder(salesOrder);
       shipment.setCarrier(carrier);
       shipment.setTrackingNumber(request.getTrackingNumber());
       shipment.setStatus(ShipmentStatus.PLANNED);

       Shipment savedShipment = shipmentRepository.save(shipment);
       return shipmentMapper.toDto(savedShipment);

   }
    private void checkWarehouseManagerOrAdminRole(UUID actorId){
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + actorId));
        if (actor.getRole() != Role.WAREHOUSE_MANAGER && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("This operation is restricted to WAREHOUSE_MANAGER or ADMIN users.");
        }
    }
}
