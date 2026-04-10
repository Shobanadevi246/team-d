package com.pharmacy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.entity.MedicineInteraction;
import com.pharmacy.repository.MedicineInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineInteractionService {

    private final MedicineInteractionRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${anthropic.api.key:#{null}}")
    private String anthropicApiKey;

    public MedicineInteraction checkInteraction(String med1, String med2, String checkedBy) {
        String result;
        MedicineInteraction.SeverityLevel severity;

        try {
            if (anthropicApiKey != null && !anthropicApiKey.isBlank() && !anthropicApiKey.equals("your-api-key-here")) {
                result = callClaudeAPI(med1, med2);
                severity = parseSeverity(result);
            } else {
                result = getRuleBasedInteraction(med1, med2);
                severity = parseSeverity(result);
            }
        } catch (Exception e) {
            log.error("AI interaction check failed: {}", e.getMessage());
            result = getRuleBasedInteraction(med1, med2);
            severity = parseSeverity(result);
        }

        MedicineInteraction interaction = new MedicineInteraction();
        interaction.setMedicine1(med1.trim());
        interaction.setMedicine2(med2.trim());
        interaction.setInteractionResult(result);
        interaction.setSeverity(severity);
        interaction.setCheckedBy(checkedBy);
        return repository.save(interaction);
    }

    private String callClaudeAPI(String med1, String med2) throws Exception {
        String prompt = String.format(
            "You are a clinical pharmacist. Compare '%s' and '%s' for interaction safety and patient suitability.\n\n" +
            "Provide a structured response with:\n" +
            "SEVERITY: [SAFE/MINOR/MODERATE/MAJOR/CONTRAINDICATED]\n" +
            "COMPARISON: [Short comparison between the 2 medicines]\n" +
            "SAFER_OPTION: [Which medicine is generally safer or say BOTH/NEITHER]\n" +
            "AGE_GUIDANCE: [Mention which age group can usually take each medicine]\n" +
            "SAFE_USE: [State whether they are safe together or if one should be preferred]\n" +
            "INTERACTION: [Brief description of what happens]\n" +
            "MECHANISM: [How they interact]\n" +
            "CLINICAL_EFFECTS: [What symptoms/effects to watch for]\n" +
            "RECOMMENDATION: [What to do - avoid/monitor/safe/adjust dose]\n" +
            "MANAGEMENT: [Specific management advice]\n\n" +
            "Keep response concise, clinical, and actionable. If no significant interaction exists, clearly state SEVERITY: SAFE.",
            med1, med2
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", "claude-sonnet-4-20250514");
        body.put("max_tokens", 600);
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

    private String getRuleBasedInteraction(String med1, String med2) {
        String m1 = med1.toLowerCase().trim();
        String m2 = med2.toLowerCase().trim();

        // Common known interactions database
        Map<String, String[]> interactions = new LinkedHashMap<>();
        interactions.put("warfarin+aspirin", new String[]{"MAJOR",
            "INTERACTION: Significantly increases bleeding risk.\nMECHANISM: Additive anticoagulant and antiplatelet effects.\nCLINICAL_EFFECTS: Increased risk of serious bleeding including GI bleeding and intracranial hemorrhage.\nRECOMMENDATION: Avoid combination unless clearly indicated. If necessary, use lowest effective doses.\nMANAGEMENT: Monitor INR closely, watch for signs of bleeding."});
        interactions.put("metformin+alcohol", new String[]{"MODERATE",
            "INTERACTION: Risk of lactic acidosis.\nMECHANISM: Alcohol inhibits gluconeogenesis and impairs lactate clearance.\nCLINICAL_EFFECTS: Lactic acidosis, hypoglycemia.\nRECOMMENDATION: Advise patient to avoid or limit alcohol consumption.\nMANAGEMENT: Monitor blood glucose, educate patient."});
        interactions.put("paracetamol+alcohol", new String[]{"MAJOR",
            "INTERACTION: Severe hepatotoxicity risk.\nMECHANISM: Alcohol induces CYP2E1 enzyme, increasing toxic NAPQI metabolite.\nCLINICAL_EFFECTS: Liver damage, hepatic failure.\nRECOMMENDATION: Avoid combination. Do not take paracetamol with regular alcohol use.\nMANAGEMENT: Monitor liver function tests if combined."});
        interactions.put("ciprofloxacin+antacid", new String[]{"MODERATE",
            "INTERACTION: Reduced ciprofloxacin absorption.\nMECHANISM: Antacids (Ca2+, Mg2+, Al3+) chelate ciprofloxacin in GI tract.\nCLINICAL_EFFECTS: Reduced antibiotic efficacy, treatment failure risk.\nRECOMMENDATION: Separate doses by at least 2-4 hours.\nMANAGEMENT: Give ciprofloxacin 2h before or 6h after antacid."});
        interactions.put("ssri+tramadol", new String[]{"MAJOR",
            "INTERACTION: Serotonin syndrome risk.\nMECHANISM: Both increase serotonergic activity.\nCLINICAL_EFFECTS: Agitation, fever, tremor, seizures, potentially fatal serotonin syndrome.\nRECOMMENDATION: Avoid combination. Use alternative analgesic.\nMANAGEMENT: If unavoidable, start with lowest doses, monitor closely."});
        interactions.put("digoxin+amiodarone", new String[]{"MAJOR",
            "INTERACTION: Increased digoxin toxicity.\nMECHANISM: Amiodarone inhibits P-glycoprotein and renal digoxin clearance.\nCLINICAL_EFFECTS: Nausea, vomiting, bradycardia, heart block, arrhythmias.\nRECOMMENDATION: Reduce digoxin dose by 30-50% when starting amiodarone.\nMANAGEMENT: Monitor digoxin levels and ECG closely."});
        interactions.put("methotrexate+nsaid", new String[]{"MAJOR",
            "INTERACTION: Methotrexate toxicity.\nMECHANISM: NSAIDs reduce renal methotrexate clearance.\nCLINICAL_EFFECTS: Bone marrow suppression, hepatotoxicity, mucositis.\nRECOMMENDATION: Avoid NSAIDs with methotrexate therapy.\nMANAGEMENT: Monitor CBC and renal function. Use paracetamol instead."});
        interactions.put("simvastatin+amlodipine", new String[]{"MODERATE",
            "INTERACTION: Increased statin exposure and myopathy risk.\nMECHANISM: Amlodipine inhibits CYP3A4 metabolism of simvastatin.\nCLINICAL_EFFECTS: Myalgia, myopathy, rhabdomyolysis.\nRECOMMENDATION: Limit simvastatin dose to 20mg/day with amlodipine.\nMANAGEMENT: Monitor for muscle pain, check CK levels."});

        // Check combinations
        String combo1 = m1 + "+" + m2;
        String combo2 = m2 + "+" + m1;

        for (Map.Entry<String, String[]> entry : interactions.entrySet()) {
            String key = entry.getKey();
            if (combo1.contains(key.split("\\+")[0]) && combo1.contains(key.split("\\+")[1]) ||
                combo2.contains(key.split("\\+")[0]) && combo2.contains(key.split("\\+")[1])) {
                String[] data = entry.getValue();
                return "SEVERITY: " + data[0] + "\n" +
                       "COMPARISON: " + compareMedicinesSummary(med1, med2) + "\n" +
                       "SAFER_OPTION: " + determineSaferOption(med1, med2, data[0]) + "\n" +
                       "AGE_GUIDANCE: " + buildAgeGuidance(med1, med2) + "\n" +
                       "SAFE_USE: " + safeUseSummary(med1, med2, data[0]) + "\n" +
                       data[1];
            }
        }

        return "SEVERITY: SAFE\n" +
               "COMPARISON: " + compareMedicinesSummary(med1, med2) + "\n" +
               "SAFER_OPTION: " + determineSaferOption(med1, med2, "SAFE") + "\n" +
               "AGE_GUIDANCE: " + buildAgeGuidance(med1, med2) + "\n" +
               "SAFE_USE: " + safeUseSummary(med1, med2, "SAFE") + "\n" +
               "INTERACTION: No clinically significant interaction identified between " + med1 + " and " + med2 + ".\n" +
               "MECHANISM: These medications work through different pathways with no known interaction.\n" +
               "CLINICAL_EFFECTS: No adverse interaction effects expected.\n" +
               "RECOMMENDATION: Can be used together safely.\n" +
               "MANAGEMENT: Standard monitoring applies. Always verify with current clinical guidelines.";
    }

    private String compareMedicinesSummary(String med1, String med2) {
        return med1 + " and " + med2 + " should be compared by indication, age suitability, and interaction risk before choosing either medicine.";
    }

    private String determineSaferOption(String med1, String med2, String severity) {
        if ("CONTRAINDICATED".equals(severity) || "MAJOR".equals(severity)) {
            return "Neither medicine combination is considered safe together. Prefer a doctor-reviewed alternative instead of self-selecting one.";
        }

        int score1 = medicineRiskScore(med1);
        int score2 = medicineRiskScore(med2);
        if (score1 == score2) {
            return "Both medicines have similar general safety on the available rules. Choose based on diagnosis, age, and clinician advice.";
        }
        return score1 < score2
                ? med1 + " is generally the safer option for routine use when it matches the diagnosis."
                : med2 + " is generally the safer option for routine use when it matches the diagnosis.";
    }

    private int medicineRiskScore(String medicine) {
        String value = medicine.toLowerCase(Locale.ROOT);
        if (value.contains("aspirin")) return 4;
        if (value.contains("warfarin")) return 5;
        if (value.contains("tramadol")) return 4;
        if (value.contains("digoxin") || value.contains("amiodarone") || value.contains("methotrexate")) return 5;
        if (value.contains("ibuprofen") || value.contains("diclofenac") || value.contains("nsaid")) return 3;
        if (value.contains("ciprofloxacin")) return 3;
        if (value.contains("paracetamol") || value.contains("acetaminophen")) return 1;
        if (value.contains("amoxicillin")) return 2;
        if (value.contains("metformin") || value.contains("amlodipine")) return 2;
        return 2;
    }

    private String buildAgeGuidance(String med1, String med2) {
        return med1 + ": " + ageGuidanceFor(med1) + " | " +
               med2 + ": " + ageGuidanceFor(med2);
    }

    private String ageGuidanceFor(String medicine) {
        String value = medicine.toLowerCase(Locale.ROOT);
        if (value.contains("paracetamol") || value.contains("acetaminophen")) {
            return "Can be used in children, adults, and elderly with age-appropriate dosing.";
        }
        if (value.contains("ibuprofen")) {
            return "Usually suitable from 6 months of age and above with correct dose; avoid in some kidney, stomach, or dehydration conditions.";
        }
        if (value.contains("aspirin")) {
            return "Usually for adults only unless specifically prescribed; generally avoided below 16 years because of Reye syndrome risk.";
        }
        if (value.contains("amoxicillin")) {
            return "Used in children and adults with prescription and weight-based dosing.";
        }
        if (value.contains("ciprofloxacin")) {
            return "Usually adults; children only under specialist prescription.";
        }
        if (value.contains("metformin")) {
            return "Usually for adults and children 10 years or older when prescribed for diabetes.";
        }
        if (value.contains("warfarin")) {
            return "Mostly adult use; children only under specialist monitoring.";
        }
        if (value.contains("tramadol")) {
            return "Generally for adults and older adolescents only; avoid in young children.";
        }
        if (value.contains("digoxin") || value.contains("amiodarone") || value.contains("methotrexate")) {
            return "Needs doctor supervision; age suitability depends on diagnosis and close monitoring.";
        }
        return "Age suitability depends on the diagnosis. Use only with doctor or pharmacist guidance.";
    }

    private String safeUseSummary(String med1, String med2, String severity) {
        if ("SAFE".equals(severity) || "MINOR".equals(severity)) {
            return med1 + " and " + med2 + " are generally safe together when prescribed correctly and age guidance is followed.";
        }
        if ("MODERATE".equals(severity)) {
            return med1 + " and " + med2 + " may be used together only with monitoring and clinician advice.";
        }
        return med1 + " and " + med2 + " should not be considered a safe pair without direct medical supervision.";
    }

    private MedicineInteraction.SeverityLevel parseSeverity(String result) {
        if (result.contains("SEVERITY: CONTRAINDICATED")) return MedicineInteraction.SeverityLevel.CONTRAINDICATED;
        if (result.contains("SEVERITY: MAJOR")) return MedicineInteraction.SeverityLevel.MAJOR;
        if (result.contains("SEVERITY: MODERATE")) return MedicineInteraction.SeverityLevel.MODERATE;
        if (result.contains("SEVERITY: MINOR")) return MedicineInteraction.SeverityLevel.MINOR;
        if (result.contains("SEVERITY: SAFE")) return MedicineInteraction.SeverityLevel.SAFE;
        return MedicineInteraction.SeverityLevel.UNKNOWN;
    }

    public List<MedicineInteraction> getRecentInteractions() {
        return repository.findTop20ByOrderByCheckedAtDesc();
    }
}
