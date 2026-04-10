package com.pharmacy.repository;

import com.pharmacy.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(PurchaseOrder.OrderStatus status);
    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();
    List<PurchaseOrder> findByRaisedByStaffIdOrderByCreatedAtDesc(Long staffId);
    long countByStatus(PurchaseOrder.OrderStatus status);
}
