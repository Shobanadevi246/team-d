package com.pharmacy.util;

import com.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SkuGenerator {

    private final MedicineRepository medicineRepository;

    public String generateSku(String productName) {
        String prefix = productName.length() >= 3
                ? productName.substring(0, 3).toUpperCase()
                : productName.toUpperCase();
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String sku = "MED-" + prefix + "-" + unique;

        // Ensure uniqueness
        while (medicineRepository.existsBySkuId(sku)) {
            unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            sku = "MED-" + prefix + "-" + unique;
        }
        return sku;
    }
}
