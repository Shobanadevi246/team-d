package com.pharmacy.controller;

import com.pharmacy.entity.*;
import com.pharmacy.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Objects;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('STAFF')")
@RequiredArgsConstructor
@Slf4j
public class StaffController {
    private static final MediaType EXCEL_MEDIA_TYPE = Objects.requireNonNull(
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));


    private final StaffService staffService;
    private final MedicineService medicineService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;

    private Staff getCurrentStaff(Authentication auth) {
        return staffService.getStaffByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
    }

    private void addNotifications(Model model) {
        model.addAttribute("lowStockCount", medicineService.getLowStockCount());
        model.addAttribute("expiringCount", medicineService.getExpiringCount());
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Staff staff = getCurrentStaff(auth);
        addNotifications(model);
        model.addAttribute("staff", staff);
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        long myMedicinesCount = medicineService.getAllMedicines().stream()
                .filter(m -> m.getAddedByStaff() != null && m.getAddedByStaff().getId().equals(staff.getId())).count();
        model.addAttribute("myMedicines", myMedicinesCount);
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        model.addAttribute("expiringMedicines", medicineService.getExpiringMedicines());
        model.addAttribute("recentTransactions", medicineService.getTransactionsByStaff(staff.getId()).stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(5).toList());
        return "staff/dashboard";
    }

    @GetMapping("/medicines")
    public String medicineList(Authentication auth, Model model) {
        addNotifications(model);
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("staff", getCurrentStaff(auth));
        return "staff/medicine-list";
    }

    @GetMapping("/medicines/add")
    public String addMedicinePage(Authentication auth, Model model) {
        addNotifications(model);
        model.addAttribute("staff", getCurrentStaff(auth));
        return "staff/medicine-add";
    }

    @PostMapping("/medicines/add")
    public String addMedicine(Authentication auth,
                              @RequestParam String productName,
                              @RequestParam(required = false) String skuId,
                              @RequestParam(required = false) MultipartFile imageFile,
                              @RequestParam int stockQuantity,
                              @RequestParam(required = false) String expiryDate,
                              RedirectAttributes ra) {
        try {
            Staff staff = getCurrentStaff(auth);
            Medicine medicine = new Medicine();
            medicine.setProductName(productName);
            medicine.setSkuId(skuId);
            medicine.setStockQuantity(stockQuantity);
            if (expiryDate != null && !expiryDate.isBlank()) {
                medicine.setExpiryDate(LocalDate.parse(expiryDate));
            }
            medicineService.addMedicine(medicine, imageFile, staff, false);
            ra.addFlashAttribute("success", "Medicine added successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/staff/medicines";
    }

    @GetMapping("/medicines/{id}")
    public String medicineDetails(@PathVariable Long id, Authentication auth, Model model) {
        addNotifications(model);
        medicineService.getMedicineById(id).ifPresentOrElse(m -> {
            model.addAttribute("medicine", m);
            model.addAttribute("transactions", medicineService.getTransactionsByMedicine(id));
        }, () -> model.addAttribute("error", "Medicine not found"));
        model.addAttribute("staff", getCurrentStaff(auth));
        return "staff/medicine-details";
    }

    @GetMapping("/stock")
    public String stockPage(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = getCurrentStaff(auth);
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("staff", staff);
        model.addAttribute("transactions", medicineService.getTransactionsByStaff(staff.getId()).stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(50).toList());
        return "staff/stock";
    }

    @PostMapping("/stock/in")
    public String stockIn(Authentication auth,
                          @RequestParam Long medicineId,
                          @RequestParam int quantity,
                          @RequestParam(required = false) String expiryDate,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes ra) {
        try {
            Staff staff = getCurrentStaff(auth);
            LocalDate expiry = (expiryDate != null && !expiryDate.isBlank()) ? LocalDate.parse(expiryDate) : null;
            medicineService.stockIn(medicineId, quantity, expiry, false, staff, notes);
            ra.addFlashAttribute("success", "Stock IN recorded successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/staff/stock";
    }

    @PostMapping("/stock/out")
    public String stockOut(Authentication auth,
                           @RequestParam Long medicineId,
                           @RequestParam int quantity,
                           @RequestParam(required = false) String notes,
                           RedirectAttributes ra) {
        try {
            Staff staff = getCurrentStaff(auth);
            medicineService.stockOut(medicineId, quantity, false, staff, notes);
            ra.addFlashAttribute("success", "Stock OUT recorded successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/staff/stock";
    }

    @GetMapping("/low-stock")
    public String lowStock(Authentication auth, Model model) {
        addNotifications(model);
        model.addAttribute("medicines", medicineService.getLowStockMedicines());
        model.addAttribute("staff", getCurrentStaff(auth));
        return "staff/low-stock";
    }

    @GetMapping("/expiry-alert")
    public String expiryAlert(Authentication auth, Model model) {
        addNotifications(model);
        model.addAttribute("expiringMedicines", medicineService.getExpiringMedicines());
        model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines());
        model.addAttribute("staff", getCurrentStaff(auth));
        return "staff/expiry-alert";
    }

    @GetMapping("/feedback")
    public String feedbackPage(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = getCurrentStaff(auth);
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        model.addAttribute("staff", staff);
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "staff/feedback";
    }

    @PostMapping("/feedback/add")
    public String addFeedback(Authentication auth,
                              @RequestParam String customerName,
                              @RequestParam String customerType,
                              @RequestParam String medicineName,
                              @RequestParam String medicineBrand,
                              @RequestParam String review,
                              @RequestParam(defaultValue = "5") int rating,
                              RedirectAttributes ra) {
        try {
            Staff staff = getCurrentStaff(auth);
            CustomerFeedback fb = new CustomerFeedback();
            fb.setCustomerName(customerName);
            fb.setCustomerType(CustomerFeedback.CustomerType.valueOf(customerType));
            fb.setMedicineName(medicineName);
            fb.setMedicineBrand(medicineBrand);
            fb.setReview(review);
            fb.setRating(rating);
            feedbackService.saveFeedback(fb, false, staff);
            ra.addFlashAttribute("success", "Feedback submitted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/staff/feedback";
    }

    @GetMapping("/reports")
    public String reportsPage(Authentication auth, Model model) {
        addNotifications(model);
        model.addAttribute("staff", getCurrentStaff(auth));
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        model.addAttribute("totalFeedback", feedbackService.getAllFeedback().size());
        model.addAttribute("fastSelling", medicineService.getFastSellingMedicines().stream().limit(5).toList());
        model.addAttribute("slowSelling", medicineService.getSlowSellingMedicines().stream().limit(5).toList());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        return "staff/reports";
    }

    @GetMapping("/reports/download/medicine/pdf")
    public ResponseEntity<byte[]> downloadMedicinePdf() throws Exception {
        byte[] data = reportService.generateMedicinePdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicine-report.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(data);
    }

    @GetMapping("/reports/download/medicine/excel")
    public ResponseEntity<byte[]> downloadMedicineExcel() throws Exception {
        byte[] data = reportService.generateMedicineExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicine-report.xlsx")
                .contentType(EXCEL_MEDIA_TYPE)
                .body(data);
    }

    @GetMapping("/reports/download/feedback/pdf")
    public ResponseEntity<byte[]> downloadFeedbackPdf() throws Exception {
        byte[] data = reportService.generateFeedbackPdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feedback-report.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(data);
    }

    @GetMapping("/reports/download/feedback/excel")
    public ResponseEntity<byte[]> downloadFeedbackExcel() throws Exception {
        byte[] data = reportService.generateFeedbackExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feedback-report.xlsx")
                .contentType(EXCEL_MEDIA_TYPE)
                .body(data);
    }
}
