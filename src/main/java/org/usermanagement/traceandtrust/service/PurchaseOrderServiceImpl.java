package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.*;
import org.usermanagement.traceandtrust.entity.*;
import org.usermanagement.traceandtrust.entity.PurchaseOrderLine;
import org.usermanagement.traceandtrust.enums.MovementType;
import org.usermanagement.traceandtrust.enums.PurchaseOrderStatus;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.exception.BusinessException;
import org.usermanagement.traceandtrust.exception.ForbiddenAccessException;
import org.usermanagement.traceandtrust.exception.ResourceNotFoundException;
import org.usermanagement.traceandtrust.mapper.PurchaseOrderMapper;
import org.usermanagement.traceandtrust.repository.*;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryService inventoryService;
    private final WarehouseRepository warehouseRepository;

    public PurchaseOrderDto createPurshOrder(CreatePurchaseOrderRequest request){


        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);

        for(PurchaseOrderLineDto lineDto : request.getOrderLines()){

            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + lineDto.getProductId()));

            PurchaseOrderLine ligne = new PurchaseOrderLine();
            ligne.setProduct(product);
            ligne.setQuantityOrdered(lineDto.getQuantityOrdered());
            ligne.setUnitPrice(lineDto.getUnitPrice());

            purchaseOrder.addLine(ligne);
        }

        PurchaseOrder purshorderSaved = purchaseOrderRepository.save(purchaseOrder);

         return purchaseOrderMapper.toDto(purshorderSaved);
    }

    @Transactional
    public PurchaseOrderDto receivePurchaseOrderItems(UUID purchaseOrderId, ReceivePurchaseOrderRequest request){
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + purchaseOrderId));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED || po.getStatus() == PurchaseOrderStatus.CANCELED) {
            throw new BusinessException("Purchase order is already closed or canceled.");
        }
        for(ReceivePurchaseOrderLineDto recieveline : request.getReceivedLines()){

            PurchaseOrderLine line = po.getOrderLines().stream().filter(l -> l.getId().equals(recieveline.getPurchaseOrderlinId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Order line with id " + recieveline.getPurchaseOrderlinId() + " not found in this purchase order."));
            int totalReceivedSoFar = line.getQuantityReceived() + recieveline.getQuantityReceived();
            if (totalReceivedSoFar > line.getQuantityOrdered()) {
                throw new BusinessException("Cannot receive more items than ordered for product " + line.getProduct().getSku());
            }
            CreateMovementRequest movementRequest = new CreateMovementRequest();
            movementRequest.setProductId(line.getProduct().getId());
            movementRequest.setWarehouseId(warehouse.getId());
            movementRequest.setType(MovementType.INBOUND);
            movementRequest.setQuantity(recieveline.getQuantityReceived());
            inventoryService.recordMovement(movementRequest);
            line.setQuantityReceived(totalReceivedSoFar);
        }
        updatePurchaseOrderStatus(po);

        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        return purchaseOrderMapper.toDto(savedPo);
    }


    private void updatePurchaseOrderStatus(PurchaseOrder po) {
        boolean allLinesFullyReceived = po.getOrderLines().stream()
                .allMatch(line -> line.getQuantityReceived() >= line.getQuantityOrdered());

        if (allLinesFullyReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
        } else {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
    }

}
