package com.pharmacy.controller;

import com.pharmacy.entity.*;
import com.pharmacy.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UniqueFeatureController {

    private final MedicineInteractionService interactionService;
    private final SmartReorderPredictorService reorderPredictorService;
    private final ShiftHandoverService shiftHandoverService;
    private final ChatService chatService;
    private final MedicineService medicineService;
    private final StaffService staffService;
    private final AuditLogService auditLogService;

    private void addNotifications(Model model) {
        model.addAttribute("lowStockCount", medicineService.getLowStockCount());
        model.addAttribute("expiringCount", medicineService.getExpiringCount());
    }

    // ═══════════════════════════════════════════════
    // 1. AI DRUG INTERACTION CHECKER
    // ═══════════════════════════════════════════════

    @GetMapping("/admin/interaction-checker")
    public String interactionCheckerPage(Model model) {
        addNotifications(model);
        model.addAttribute("recentChecks", interactionService.getRecentInteractions());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "admin/interaction-checker";
    }

    @PostMapping("/admin/interaction-checker/check")
    @ResponseBody
    public Map<String, Object> checkInteraction(@RequestParam String medicine1,
                                                 @RequestParam String medicine2,
                                                 Authentication auth) {
        try {
            MedicineInteraction result = interactionService.checkInteraction(
                    medicine1, medicine2, auth.getName());
            auditLogService.log("Drug Interaction Check", auth.getName(), "ADMIN",
                    medicine1 + " + " + medicine2 + " -> " + result.getSeverity());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medicine1", result.getMedicine1());
            response.put("medicine2", result.getMedicine2());
            response.put("severity", result.getSeverity().name());
            response.put("result", result.getInteractionResult());
            response.put("id", result.getId());
            return response;
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @GetMapping("/staff/interaction-checker")
    public String staffInteractionChecker(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
        model.addAttribute("staff", staff);
        model.addAttribute("recentChecks", interactionService.getRecentInteractions());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "staff/interaction-checker";
    }

    @PostMapping("/staff/interaction-checker/check")
    @ResponseBody
    public Map<String, Object> staffCheckInteraction(@RequestParam String medicine1,
                                                      @RequestParam String medicine2,
                                                      Authentication auth) {
        return checkInteraction(medicine1, medicine2, auth);
    }

    // ═══════════════════════════════════════════════
    // 2. SMART REORDER PREDICTOR
    // ═══════════════════════════════════════════════

    @GetMapping("/admin/smart-predictor")
    public String smartPredictor(Model model) {
        addNotifications(model);
        model.addAttribute("predictions", reorderPredictorService.generatePredictions());
        model.addAttribute("heatmapData", reorderPredictorService.generateDemandHeatmap());
        return "admin/smart-predictor";
    }

    @GetMapping("/admin/smart-predictor/api")
    @ResponseBody
    public List<Map<String, Object>> smartPredictorApi() {
        return reorderPredictorService.generatePredictions();
    }

    @GetMapping("/admin/smart-predictor/heatmap")
    @ResponseBody
    public Map<String, Object> heatmapData() {
        return reorderPredictorService.generateDemandHeatmap();
    }

    @GetMapping("/staff/smart-predictor")
    public String staffSmartPredictor(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
        model.addAttribute("staff", staff);
        model.addAttribute("predictions", reorderPredictorService.generatePredictions());
        return "staff/smart-predictor";
    }

    // ═══════════════════════════════════════════════
    // 3. SHIFT HANDOVER SYSTEM
    // ═══════════════════════════════════════════════

    @GetMapping("/admin/shift-handover")
    public String adminShiftHandover(Model model) {
        addNotifications(model);
        model.addAttribute("handovers", shiftHandoverService.getAllHandovers());
        model.addAttribute("staffList", staffService.getAllStaff());
        model.addAttribute("lowStockCount", medicineService.getLowStockCount());
        model.addAttribute("expiringCount", medicineService.getExpiringCount());
        return "admin/shift-handover";
    }

    @GetMapping("/staff/shift-handover")
    public String staffShiftHandover(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
        model.addAttribute("staff", staff);
        model.addAttribute("myHandovers", shiftHandoverService.getHandoversByStaff(staff.getId()));
        model.addAttribute("allStaff", staffService.getAllStaff());
        model.addAttribute("lowStockCount", medicineService.getLowStockCount());
        model.addAttribute("expiringCount", medicineService.getExpiringCount());
        return "staff/shift-handover";
    }

    @PostMapping("/staff/shift-handover/create")
    public String createHandover(Authentication auth,
                                  @RequestParam Long incomingStaffId,
                                  @RequestParam String shiftType,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes ra) {
        try {
            Staff outgoing = staffService.getStaffByEmail(auth.getName()).orElseThrow();
            shiftHandoverService.createHandover(outgoing.getId(), incomingStaffId, shiftType, notes);
            auditLogService.log("Shift Handover Created", auth.getName(), "STAFF",
                    "Shift: " + shiftType);
            ra.addFlashAttribute("success", "Shift handover submitted with AI summary!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/staff/shift-handover";
    }

    @PostMapping("/staff/shift-handover/{id}/acknowledge")
    public String acknowledgeHandover(@PathVariable Long id, RedirectAttributes ra) {
        shiftHandoverService.acknowledgeHandover(id);
        ra.addFlashAttribute("success", "Handover acknowledged!");
        return "redirect:/staff/shift-handover";
    }

    // ═══════════════════════════════════════════════
    // ═══════════════════════════════════════════════
    // 4. REAL-TIME STAFF CHAT (SSE)
    // ═══════════════════════════════════════════════

    @GetMapping("/admin/chat")
    public String adminChat(Model model) {
        addNotifications(model);
        model.addAttribute("messages", chatService.getRecentMessages());
        return "admin/chat";
    }

    @GetMapping("/staff/chat")
    public String staffChat(Authentication auth, Model model) {
        addNotifications(model);
        Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
        model.addAttribute("staff", staff);
        model.addAttribute("messages", chatService.getRecentMessages());
        return "staff/chat";
    }

    @GetMapping(value = "/chat/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeChatSSE() {
        return chatService.subscribe();
    }

    @PostMapping("/admin/chat/send")
    @ResponseBody
    public Map<String, Object> adminSendMessage(@RequestParam String message,
                                                 @RequestParam(defaultValue = "false") boolean announcement) {
        try {
            ChatMessage msg = chatService.sendMessage("Administrator", "admin", "ADMIN", message, announcement);
            return Map.of("success", true, "id", msg.getId());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @PostMapping("/staff/chat/send")
    @ResponseBody
    public Map<String, Object> staffSendMessage(Authentication auth,
                                                  @RequestParam String message) {
        try {
            Staff staff = staffService.getStaffByEmail(auth.getName()).orElseThrow();
            ChatMessage msg = chatService.sendMessage(staff.getStaffName(), auth.getName(), "STAFF", message, false);
            return Map.of("success", true, "id", msg.getId());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}
