package com.pharmacy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.entity.*;
import com.pharmacy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftHandoverService {

    private final ShiftHandoverRepository handoverRepository;
    private final MedicineService medicineService;
    private final StaffService staffService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${anthropic.api.key:#{null}}")
    private String anthropicApiKey;

    @Transactional
    public ShiftHandover createHandover(Long outgoingStaffId, Long incomingStaffId,
                                         String shiftType, String notes) {
        ShiftHandover handover = new ShiftHandover();

        staffService.getStaffById(outgoingStaffId).ifPresent(handover::setOutgoingStaff);
        staffService.getStaffById(incomingStaffId).ifPresent(handover::setIncomingStaff);

        handover.setShiftType(shiftType);
        handover.setHandoverNotes(notes);

        // Auto-collect stats
        handover.setLowStockCount(medicineService.getLowStockCount());
        handover.setExpiringCount(medicineService.getExpiringCount());

        // Collect critical alerts
        StringBuilder alerts = new StringBuilder();
        medicineService.getLowStockMedicines().forEach(m ->
            alerts.append("LOW STOCK: ").append(m.getProductName())
                  .append(" (").append(m.getStockQuantity()).append(" units)\n"));
        medicineService.getExpiringMedicines().forEach(m ->
            alerts.append("EXPIRING: ").append(m.getProductName())
                  .append(" (expires ").append(m.getExpiryDate()).append(")\n"));
        handover.setCriticalAlerts(alerts.toString());

        // Generate AI summary
        String aiSummary = generateAISummary(handover, notes);
        handover.setAiSummary(aiSummary);

        handover.setStatus(ShiftHandover.HandoverStatus.PENDING);
        return handoverRepository.save(handover);
    }

    private String generateAISummary(ShiftHandover handover, String notes) {
        try {
            if (anthropicApiKey != null && !anthropicApiKey.isBlank() && !anthropicApiKey.equals("your-api-key-here")) {
                String prompt = String.format(
                    "You are a pharmacy shift supervisor. Create a concise, professional shift handover summary based on:\n\n" +
                    "Shift Type: %s\n" +
                    "Low Stock Items: %d medicines below 10 units\n" +
                    "Expiring Soon: %d medicines expiring within 10 days\n" +
                    "Critical Alerts:\n%s\n" +
                    "Staff Notes: %s\n\n" +
                    "Write a 3-4 sentence professional handover summary highlighting key actions needed for the incoming shift. " +
                    "Be specific about priorities.",
                    handover.getShiftType(),
                    handover.getLowStockCount(),
                    handover.getExpiringCount(),
                    handover.getCriticalAlerts(),
                    notes != null ? notes : "No additional notes"
                );

                Map<String, Object> body = new HashMap<>();
                body.put("model", "claude-sonnet-4-20250514");
                body.put("max_tokens", 300);
                body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-api-key", anthropicApiKey);
                headers.set("anthropic-version", "2023-06-01");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.anthropic.com/v1/messages", request, String.class);

                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("content").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.warn("AI summary generation failed: {}", e.getMessage());
        }

        // Fallback rule-based summary
        return String.format(
            "%s shift handover initiated. Current pharmacy status: %d medicines require immediate restocking " +
            "(below 10 units) and %d medicines approaching expiry within 10 days. " +
            "Incoming staff should prioritize reviewing the %s section. %s",
            handover.getShiftType(),
            handover.getLowStockCount(),
            handover.getExpiringCount(),
            handover.getLowStockCount() > 0 ? "low stock" : "expiry alerts",
            notes != null && !notes.isBlank() ? "Additional note: " + notes : "All systems operational."
        );
    }

    @Transactional
    public void acknowledgeHandover(Long handoverId) {
        if (handoverId != null) {
            handoverRepository.findById(handoverId).ifPresent(h -> {
                h.setStatus(ShiftHandover.HandoverStatus.ACKNOWLEDGED);
                h.setAcknowledgedAt(java.time.LocalDateTime.now());
                handoverRepository.save(h);
            });
        }
    }

    public List<ShiftHandover> getAllHandovers() {
        return handoverRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ShiftHandover> getHandoversByStaff(Long staffId) {
        return handoverRepository.findByOutgoingStaffIdOrderByCreatedAtDesc(staffId);
    }
}
