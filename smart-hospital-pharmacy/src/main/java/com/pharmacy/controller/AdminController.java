package com.pharmacy.controller;

import com.pharmacy.entity.*;
import com.pharmacy.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Objects;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private static final MediaType EXCEL_MEDIA_TYPE = Objects.requireNonNull(
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));


    private final StaffService staffService;
    private final MedicineService medicineService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;

    private void addNotifications(Model model) {
        model.addAttribute("lowStockCount", medicineService.getLowStockCount());
        model.addAttribute("expiringCount", medicineService.getExpiringCount());
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        addNotifications(model);
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        model.addAttribute("totalStaff", staffService.getAllStaff().size());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        model.addAttribute("expiringMedicines", medicineService.getExpiringMedicines());
        model.addAttribute("recentTransactions", medicineService.getAllTransactions().stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(5).toList());
        return "admin/dashboard";
    }

    // ADMIN ACTIVITIES
    @GetMapping("/activities")
    public String adminActivities(Model model) {
        addNotifications(model);
        model.addAttribute("adminTransactions", medicineService.getAdminTransactions());
        model.addAttribute("adminMedicines", medicineService.getAllMedicines().stream()
                .filter(Medicine::isAdminAdded).toList());
        return "admin/activities";
    }

    // STAFF MANAGEMENT
    @GetMapping("/staff")
    public String staffList(Model model) {
        addNotifications(model);
        model.addAttribute("staffList", staffService.getAllStaff());
        return "admin/staff-list";
    }

    @GetMapping("/staff/add")
    public String addStaffPage(Model model) {
        addNotifications(model);
        model.addAttribute("staff", new Staff());
        return "admin/staff-add";
    }

    @PostMapping("/staff/add")
    public String addStaff(@ModelAttribute Staff staff, RedirectAttributes ra) {
        try {
            if (!staffService.isPasswordStrong(staff.getPassword())) {
                ra.addFlashAttribute("error", "Password must contain at least 1 uppercase, 1 number and 1 special character (min 8 chars).");
                return "redirect:/admin/staff/add";
            }
            staffService.createStaff(staff);
            ra.addFlashAttribute("success", "Staff member registered successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/staff/{id}")
    public String staffDetails(@PathVariable Long id, Model model) {
        addNotifications(model);
        staffService.getStaffById(id).ifPresentOrElse(s -> {
            model.addAttribute("staff", s);
            model.addAttribute("staffMedicines", medicineService.getAllMedicines().stream()
                    .filter(m -> m.getAddedByStaff() != null && m.getAddedByStaff().getId().equals(id)).toList());
            model.addAttribute("staffTransactions", medicineService.getTransactionsByStaff(id));
        }, () -> model.addAttribute("error", "Staff not found"));
        return "admin/staff-details";
    }

    @PostMapping("/staff/{id}/unlock")
    public String unlockStaff(@PathVariable Long id, RedirectAttributes ra) {
        staffService.unlockStaff(id);
        ra.addFlashAttribute("success", "Staff account unlocked successfully.");
        return "redirect:/admin/locked-accounts";
    }

    @GetMapping("/locked-accounts")
    public String lockedAccounts(Model model) {
        addNotifications(model);
        model.addAttribute("lockedStaff", staffService.getLockedStaff());
        return "admin/locked-accounts";
    }

    // MEDICINE MANAGEMENT
    @GetMapping("/medicines")
    public String medicineList(Model model) {
        addNotifications(model);
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "admin/medicine-list";
    }

    @GetMapping("/medicines/add")
    public String addMedicinePage(Model model) {
        addNotifications(model);
        return "admin/medicine-add";
    }

    @PostMapping("/medicines/add")
    public String addMedicine(@RequestParam String productName,
                              @RequestParam(required = false) String skuId,
                              @RequestParam(required = false) MultipartFile imageFile,
                              @RequestParam int stockQuantity,
                              @RequestParam(required = false) String expiryDate,
                              RedirectAttributes ra) {
        try {
            Medicine medicine = new Medicine();
            medicine.setProductName(productName);
            medicine.setSkuId(skuId);
            medicine.setStockQuantity(stockQuantity);
            if (expiryDate != null && !expiryDate.isBlank()) {
                medicine.setExpiryDate(LocalDate.parse(expiryDate));
            }
            medicineService.addMedicine(medicine, imageFile, null, true);
            ra.addFlashAttribute("success", "Medicine added successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @GetMapping("/medicines/{id}")
    public String medicineDetails(@PathVariable Long id, Model model) {
        addNotifications(model);
        medicineService.getMedicineById(id).ifPresentOrElse(m -> {
            model.addAttribute("medicine", m);
            model.addAttribute("transactions", medicineService.getTransactionsByMedicine(id));
        }, () -> model.addAttribute("error", "Medicine not found"));
        return "admin/medicine-details";
    }

    // STOCK MANAGEMENT
    @GetMapping("/stock")
    public String stockPage(Model model) {
        addNotifications(model);
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("transactions", medicineService.getAllTransactions().stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(50).toList());
        return "admin/stock";
    }

    @PostMapping("/stock/in")
    public String stockIn(@RequestParam Long medicineId,
                          @RequestParam int quantity,
                          @RequestParam(required = false) String expiryDate,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes ra) {
        try {
            LocalDate expiry = (expiryDate != null && !expiryDate.isBlank()) ? LocalDate.parse(expiryDate) : null;
            medicineService.stockIn(medicineId, quantity, expiry, true, null, notes);
            ra.addFlashAttribute("success", "Stock IN recorded successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/stock";
    }

    @PostMapping("/stock/out")
    public String stockOut(@RequestParam Long medicineId,
                           @RequestParam int quantity,
                           @RequestParam(required = false) String notes,
                           RedirectAttributes ra) {
        try {
            medicineService.stockOut(medicineId, quantity, true, null, notes);
            ra.addFlashAttribute("success", "Stock OUT recorded successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/stock";
    }

    // ALERTS
    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        addNotifications(model);
        model.addAttribute("medicines", medicineService.getLowStockMedicines());
        return "admin/low-stock";
    }

    @GetMapping("/expiry-alert")
    public String expiryAlert(Model model) {
        addNotifications(model);
        model.addAttribute("expiringMedicines", medicineService.getExpiringMedicines());
        model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines());
        return "admin/expiry-alert";
    }

    // FEEDBACK
    @GetMapping("/feedback")
    public String feedbackPage(Model model) {
        addNotifications(model);
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        model.addAttribute("feedback", new CustomerFeedback());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "admin/feedback";
    }

    @PostMapping("/feedback/add")
    public String addFeedback(@RequestParam String customerName,
                              @RequestParam String customerType,
                              @RequestParam String medicineName,
                              @RequestParam String medicineBrand,
                              @RequestParam String review,
                              @RequestParam(defaultValue = "5") int rating,
                              RedirectAttributes ra) {
        try {
            CustomerFeedback fb = new CustomerFeedback();
            fb.setCustomerName(customerName);
            fb.setCustomerType(CustomerFeedback.CustomerType.valueOf(customerType));
            fb.setMedicineName(medicineName);
            fb.setMedicineBrand(medicineBrand);
            fb.setReview(review);
            fb.setRating(rating);
            feedbackService.saveFeedback(fb, true, null);
            ra.addFlashAttribute("success", "Feedback added successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/feedback";
    }

    // REPORTS
    @GetMapping("/reports")
    public String reportsPage(Model model) {
        addNotifications(model);
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        model.addAttribute("totalStaff", staffService.getAllStaff().size());
        model.addAttribute("totalFeedback", feedbackService.getAllFeedback().size());
        model.addAttribute("fastSelling", medicineService.getFastSellingMedicines().stream().limit(5).toList());
        model.addAttribute("slowSelling", medicineService.getSlowSellingMedicines().stream().limit(5).toList());
        model.addAttribute("feedbackStats", feedbackService.getMedicineRatingStats());
        model.addAttribute("staffList", staffService.getAllStaff());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        return "admin/reports";
    }

    // REPORT DOWNLOADS
    @GetMapping("/reports/download/medicine/pdf")
    public ResponseEntity<byte[]> downloadMedicinePdf() throws Exception {
        byte[] data = reportService.generateMedicinePdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicine-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/reports/download/medicine/excel")
    public ResponseEntity<byte[]> downloadMedicineExcel() throws Exception {
        byte[] data = reportService.generateMedicineExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicine-report.xlsx")
                .contentType(EXCEL_MEDIA_TYPE)
                .body(data);
    }

    @GetMapping("/reports/download/staff/pdf")
    public ResponseEntity<byte[]> downloadStaffPdf() throws Exception {
        byte[] data = reportService.generateStaffPdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=staff-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/reports/download/staff/excel")
    public ResponseEntity<byte[]> downloadStaffExcel() throws Exception {
        byte[] data = reportService.generateStaffExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=staff-report.xlsx")
                .contentType(EXCEL_MEDIA_TYPE)
                .body(data);
    }

    @GetMapping("/reports/download/feedback/pdf")
    public ResponseEntity<byte[]> downloadFeedbackPdf() throws Exception {
        byte[] data = reportService.generateFeedbackPdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feedback-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/reports/download/feedback/excel")
    public ResponseEntity<byte[]> downloadFeedbackExcel() throws Exception {
        byte[] data = reportService.generateFeedbackExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feedback-report.xlsx")
                .contentType(EXCEL_MEDIA_TYPE)
                .body(data);
    }

    @GetMapping("/reports/download/all/pdf")
    public ResponseEntity<byte[]> downloadAllPdf() throws Exception {
        byte[] data = reportService.generateAllDataPdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=complete-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/reports/download/all/excel")
    public ResponseEntity<byte[]> downloadAllExcel() throws Exception {
        byte[] data = reportService.generateAllDataExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=complete-report.xlsx")
                .contentType(EXCEL_MEDIA_TYPE)
                .body(data);
    }
}
