package com.pharmacy.service;

import com.pharmacy.entity.PurchaseOrder;
import com.pharmacy.entity.Staff;
import com.pharmacy.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final NotificationService notificationService;

    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<PurchaseOrder> getById(Long id) {
        return id == null ? Optional.empty() : purchaseOrderRepository.findById(Objects.requireNonNull(id));
    }

    public List<PurchaseOrder> getPendingOrders() {
        return purchaseOrderRepository.findByStatusOrderByCreatedAtDesc(PurchaseOrder.OrderStatus.PENDING);
    }

    public long getPendingCount() {
        return purchaseOrderRepository.countByStatus(PurchaseOrder.OrderStatus.PENDING);
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrder order, boolean isAdmin, Staff staff) {
        order.setOrderNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setRaisedByAdmin(isAdmin);
        if (!isAdmin) order.setRaisedByStaff(staff);
        order.setStatus(PurchaseOrder.OrderStatus.PENDING);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        notificationService.notifyPurchaseOrder(order.getMedicine().getProductName(), saved.getOrderNumber());
        return saved;
    }

    @Transactional
    public void updateStatus(Long id, PurchaseOrder.OrderStatus status) {
        if (id == null) {
            return;
        }
        purchaseOrderRepository.findById(Objects.requireNonNull(id)).ifPresent(order -> {
            order.setStatus(status);
            if (status == PurchaseOrder.OrderStatus.DELIVERED) {
                order.setDeliveredDate(LocalDate.now());
            }
            purchaseOrderRepository.save(order);
        });
    }

    public List<PurchaseOrder> getOrdersByStaff(Long staffId) {
        return staffId == null ? List.of() : purchaseOrderRepository.findByRaisedByStaffIdOrderByCreatedAtDesc(Objects.requireNonNull(staffId));
    }
}
