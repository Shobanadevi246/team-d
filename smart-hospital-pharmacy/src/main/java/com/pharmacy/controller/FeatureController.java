package com.pharmacy.controller;

import com.pharmacy.entity.*;
import com.pharmacy.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FeatureController {

    private final MedicineService medicineService;
    private final StaffService staffService;
    private final PurchaseOrderService purchaseOrderService;
    private final PrescriptionService prescriptionService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    private void addNotifications(Model model) {
        model.addAttribute("lowStockCount", medicineService.getLowStockCount());
        model.addAttribute("expiringCount", medicineService.getExpiringCount());
    }

    // ======================== PURCHASE ORDERS (Admin) ========================

    @GetMapping("/admin/purchase-orders")
    public String adminPurchaseOrders(Model model) {
        addNotifications(model);
        model.addAttribute("orders", purchaseOrderService.getAllOrders());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("pendingCount", purchaseOrderService.getPendingCount());
        return "admin/purchase-orders";
    }

    @PostMapping("/admin/purchase-orders/create")
    public String createPurchaseOrder(@RequestParam Long medicineId,
                                       @RequestParam int quantityOrdered,
                                       @RequestParam(required = false) String supplierName,
                                       @RequestParam(required = false) String supplierContact,
                                       @RequestParam(required = false) Double estimatedCost,
                                       @RequestParam(required = false) String expectedDeliveryDate,
                                       @RequestParam(required = false) String notes,
                                       RedirectAttributes ra) {
        try {
            PurchaseOrder order = new PurchaseOrder();
            order.setMedicine(medicineService.getMedicineById(medicineId)
                    .orElseThrow(() -> new RuntimeException("Medicine not found")));
            order.setQuantityOrdered(quantityOrdered);
            order.setSupplierName(supplierName);
            order.setSupplierContact(supplierContact);
            order.setEstimatedCost(estimatedCost);
            order.setNotes(notes);
            if (expectedDeliveryDate != null && !expectedDeliveryDate.isBlank()) {
                order.setExpectedDeliveryDate(LocalDate.parse(expectedDeliveryDate));
            }
            purchaseOrderService.createOrder(order, true, null);
            auditLogService.log("Created Purchase Order", "admin", "ADMIN",
                    "Medicine: " + order.getMedicine().getProductName() + ", Qty: " + quantityOrdered);
            ra.addFlashAttribute("success", "Purchase order created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/purchase-orders";
    }

    @PostMapping("/admin/purchase-orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     RedirectAttributes ra) {
        try {
            purchaseOrderService.updateStatus(id, PurchaseOrder.OrderStatus.valueOf(status));
            auditLogService.log("Updated Purchase Order Status", "admin", "ADMIN",
                    "Order #" + id + " -> " + status);
            ra.addFlashAttribute("success", "Order status updated to: " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/purchase-orders";
    }

    // ======================== PRESCRIPTIONS (Admin) ========================

    @GetMapping("/admin/prescriptions")
    public String adminPrescriptions(Model model) {
        addNotifications(model);
        model.addAttribute("prescriptions", prescriptionService.getAll());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("mostPrescribed", prescriptionService.getMostPrescribed());
        model.addAttribute("totalPrescriptions", prescriptionService.getTotalCount());
        return "admin/prescriptions";
    }

    @PostMapping("/admin/prescriptions/create")
    public String createPrescription(@RequestParam String patientName,
                                      @RequestParam(required = false) String patientAge,
                                      @RequestParam(required = false) String patientGender,
                                      @RequestParam(required = false) String doctorName,
                                      @RequestParam(required = false) String wardNumber,
                                      @RequestParam Long medicineId,
                                      @RequestParam int quantityDispensed,
                                      @RequestParam(required = false) String dosage,
                                      @RequestParam(required = false) String frequency,
                                      @RequestParam(required = false) Integer durationDays,
                                      @RequestParam(required = false) String instructions,
                                      RedirectAttributes ra) {
        try {
            Prescription p = new Prescription();
            p.setPatientName(patientName);
            p.setPatientAge(patientAge);
            p.setPatientGender(patientGender);
            p.setDoctorName(doctorName);
            p.setWardNumber(wardNumber);
            p.setMedicine(medicineService.getMedicineById(medicineId).orElseThrow());
            p.setQuantityDispensed(quantityDispensed);
            p.setDosage(dosage);
            p.setFrequency(frequency);
            p.setDurationDays(durationDays);
            p.setInstructions(instructions);
            p.setPrescriptionDate(LocalDate.now());
            prescriptionService.createPrescription(p, true, null);
            auditLogService.log("Dispensed Prescription", "admin", "ADMIN",
                    "Patient: " + patientName + ", Medicine: " + p.getMedicineName());
            ra.addFlashAttribute("success", "Prescription dispensed successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/prescriptions";
    }

    // ======================== AUDIT LOG (Admin) ========================

    @GetMapping("/admin/audit-log")
    public String adminAuditLog(@RequestParam(defaultValue = "ALL") String filter, Model model) {
        addNotifications(model);
        if ("ADMIN".equals(filter)) {
            model.addAttribute("logs", auditLogService.getByRole("ADMIN"));
        } else if ("STAFF".equals(filter)) {
            model.addAttribute("logs", auditLogService.getByRole("STAFF"));
        } else {
            model.addAttribute("logs", auditLogService.getAll());
        }
        model.addAttribute("filter", filter);
        return "admin/audit-log";
    }

    // ======================== GLOBAL SEARCH (Admin) ========================

    @GetMapping("/admin/search")
    @ResponseBody
    public Map<String, Object> globalSearch(@RequestParam String q) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (q == null || q.length() < 2) return result;

        List<Map<String, String>> medicines = new ArrayList<>();
        medicineService.searchMedicines(q).stream().limit(5).forEach(m -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("type", "Medicine");
            item.put("icon", "fa-pills");
            item.put("title", m.getProductName());
            item.put("sub", "SKU: " + m.getSkuId() + " | Stock: " + m.getStockQuantity());
            item.put("url", "/admin/medicines/" + m.getId());
            medicines.add(item);
        });

        List<Map<String, String>> staff = new ArrayList<>();
        staffService.getAllStaff().stream()
                .filter(s -> s.getStaffName().toLowerCase().contains(q.toLowerCase())
                        || s.getEmail().toLowerCase().contains(q.toLowerCase())
                        || s.getStaffId().toLowerCase().contains(q.toLowerCase()))
                .limit(5).forEach(s -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("type", "Staff");
                    item.put("icon", "fa-user");
                    item.put("title", s.getStaffName());
                    item.put("sub", "ID: " + s.getStaffId() + " | " + s.getEmail());
                    item.put("url", "/admin/staff/" + s.getId());
                    staff.add(item);
                });

        result.put("medicines", medicines);
        result.put("staff", staff);
        result.put("total", medicines.size() + staff.size());
        return result;
    }

    // ======================== QR CODE DATA (Admin + Staff) ========================

    @GetMapping("/admin/medicines/{id}/qr-data")
    @ResponseBody
    public Map<String, String> getMedicineQrData(@PathVariable Long id) {
        Map<String, String> data = new HashMap<>();
        medicineService.getMedicineById(id).ifPresent(m -> {
            data.put("name", m.getProductName());
            data.put("sku", m.getSkuId());
            data.put("stock", String.valueOf(m.getStockQuantity()));
            data.put("expiry", m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A");
        });
        return data;
    }

    @GetMapping("/staff/medicines/{id}/qr-data")
    @ResponseBody
    public Map<String, String> getStaffMedicineQrData(@PathVariable Long id) {
        return getMedicineQrData(id);
    }

    // ======================== NOTIFICATIONS API ========================

    @PostMapping("/admin/notifications/mark-read")
    @ResponseBody
    public Map<String, Boolean> markNotificationsRead() {
        notificationService.markAllAdminRead();
        return Map.of("success", true);
    }

    @GetMapping("/admin/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications() {
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", notificationService.getAll());
        result.put("unread", notificationService.getUnreadAdminCount());
        return result;
    }

    // ======================== PURCHASE ORDERS (Staff) ========================

    @GetMapping("/staff/purchase-orders")
    public String staffPurchaseOrders(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
        model.addAttribute("orders", purchaseOrderService.getOrdersByStaff(staff.getId()));
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("staff", staff);
        return "staff/purchase-orders";
    }

    @PostMapping("/staff/purchase-orders/create")
    public String staffCreatePurchaseOrder(Authentication auth,
                                            @RequestParam Long medicineId,
                                            @RequestParam int quantityOrdered,
                                            @RequestParam(required = false) String supplierName,
                                            @RequestParam(required = false) String notes,
                                            RedirectAttributes ra) {
        try {
            Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
            PurchaseOrder order = new PurchaseOrder();
            order.setMedicine(medicineService.getMedicineById(medicineId).orElseThrow());
            order.setQuantityOrdered(quantityOrdered);
            order.setSupplierName(supplierName);
            order.setNotes(notes);
            purchaseOrderService.createOrder(order, false, staff);
            auditLogService.log("Created Purchase Order", staff.getEmail(), "STAFF",
                    "Medicine: " + order.getMedicine().getProductName());
            ra.addFlashAttribute("success", "Purchase order raised successfully! Awaiting admin approval.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/staff/purchase-orders";
    }

    // ======================== PRESCRIPTIONS (Staff) ========================

    @GetMapping("/staff/prescriptions")
    public String staffPrescriptions(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
        model.addAttribute("prescriptions", prescriptionService.getByStaff(staff.getId()));
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("staff", staff);
        return "staff/prescriptions";
    }

    @PostMapping("/staff/prescriptions/create")
    public String staffCreatePrescription(Authentication auth,
                                           @RequestParam String patientName,
                                           @RequestParam(required = false) String patientAge,
                                           @RequestParam(required = false) String patientGender,
                                           @RequestParam(required = false) String doctorName,
                                           @RequestParam(required = false) String wardNumber,
                                           @RequestParam Long medicineId,
                                           @RequestParam int quantityDispensed,
                                           @RequestParam(required = false) String dosage,
                                           @RequestParam(required = false) String frequency,
                                           @RequestParam(required = false) Integer durationDays,
                                           @RequestParam(required = false) String instructions,
                                           RedirectAttributes ra) {
        try {
            Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
            Prescription p = new Prescription();
            p.setPatientName(patientName);
            p.setPatientAge(patientAge);
            p.setPatientGender(patientGender);
            p.setDoctorName(doctorName);
            p.setWardNumber(wardNumber);
            p.setMedicine(medicineService.getMedicineById(medicineId).orElseThrow());
            p.setQuantityDispensed(quantityDispensed);
            p.setDosage(dosage);
            p.setFrequency(frequency);
            p.setDurationDays(durationDays);
            p.setInstructions(instructions);
            p.setPrescriptionDate(LocalDate.now());
            prescriptionService.createPrescription(p, false, staff);
            auditLogService.log("Dispensed Prescription", staff.getEmail(), "STAFF",
                    "Patient: " + patientName);
            ra.addFlashAttribute("success", "Prescription dispensed successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/staff/prescriptions";
    }
}
