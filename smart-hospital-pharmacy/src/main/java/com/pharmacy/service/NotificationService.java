package com.pharmacy.service;

import com.pharmacy.entity.Notification;
import com.pharmacy.entity.Staff;
import com.pharmacy.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(String title, String message,
                                    Notification.NotificationType type,
                                    String actionUrl, Staff targetStaff) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setActionUrl(actionUrl);
        n.setTargetStaff(targetStaff);
        notificationRepository.save(n);
    }

    public List<Notification> getUnreadAdminNotifications() {
        return notificationRepository.findByReadByAdminFalseOrderByCreatedAtDesc();
    }

    public List<Notification> getAll() {
        return notificationRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public long getUnreadAdminCount() {
        return notificationRepository.countByReadByAdminFalse();
    }

    public long getUnreadStaffCount(Long staffId) {
        return staffId == null ? 0L : notificationRepository.countByTargetStaffIdAndReadByStaffFalse(Objects.requireNonNull(staffId));
    }

    @Transactional
    public void markAllAdminRead() {
        notificationRepository.findByReadByAdminFalseOrderByCreatedAtDesc()
                .forEach(n -> { n.setReadByAdmin(true); notificationRepository.save(n); });
    }

    @Transactional
    public void markAdminRead(Long id) {
        if (id == null) {
            return;
        }
        notificationRepository.findById(Objects.requireNonNull(id)).ifPresent(n -> {
            n.setReadByAdmin(true);
            notificationRepository.save(n);
        });
    }

    public void notifyLowStock(String medicineName) {
        createNotification(
            "⚠️ Low Stock Alert",
            medicineName + " is running low (< 10 units). Please restock.",
            Notification.NotificationType.LOW_STOCK,
            "/admin/low-stock", null
        );
    }

    public void notifyExpiryWarning(String medicineName, String date) {
        createNotification(
            "🗓️ Expiry Warning",
            medicineName + " expires on " + date + ". Take action immediately.",
            Notification.NotificationType.EXPIRY_WARNING,
            "/admin/expiry-alert", null
        );
    }

    public void notifyNewStaff(String staffName) {
        createNotification(
            "👤 New Staff Registered",
            staffName + " has been added to the system.",
            Notification.NotificationType.NEW_STAFF,
            "/admin/staff", null
        );
    }

    public void notifyAccountLocked(String staffName) {
        createNotification(
            "🔒 Account Locked",
            staffName + "'s account has been locked due to failed login attempts.",
            Notification.NotificationType.ACCOUNT_LOCKED,
            "/admin/locked-accounts", null
        );
    }

    public void notifyPurchaseOrder(String medicineName, String orderNumber) {
        createNotification(
            "📦 New Purchase Order",
            "Purchase order " + orderNumber + " raised for " + medicineName,
            Notification.NotificationType.PURCHASE_ORDER,
            "/admin/purchase-orders", null
        );
    }
}
