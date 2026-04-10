package com.pharmacy.service;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.StockTransaction;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartReorderPredictorService {

    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public List<Map<String, Object>> generatePredictions() {
        List<Medicine> medicines = medicineRepository.findAll();
        List<Map<String, Object>> predictions = new ArrayList<>();

        for (Medicine medicine : medicines) {
            Map<String, Object> prediction = analyzeMedicine(medicine);
            if (prediction != null) {
                predictions.add(prediction);
            }
        }

        // Sort by urgency: days until stockout ascending
        predictions.sort(Comparator.comparingDouble(p -> (Double) p.getOrDefault("daysUntilStockout", 999.0)));
        return predictions;
    }

    private Map<String, Object> analyzeMedicine(Medicine medicine) {
        List<StockTransaction> transactions = stockTransactionRepository.findByMedicineId(medicine.getId());
        if (transactions.isEmpty()) return null;

        // Get stock-out transactions in last 30 days for consumption analysis
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<StockTransaction> recentOuts = transactions.stream()
            .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.STOCK_OUT)
            .filter(t -> t.getTransactionDate() != null && t.getTransactionDate().isAfter(thirtyDaysAgo))
            .collect(Collectors.toList());

        // Calculate daily consumption rate
        int totalConsumed30Days = recentOuts.stream().mapToInt(StockTransaction::getQuantity).sum();
        double dailyConsumptionRate = totalConsumed30Days / 30.0;

        // Calculate all-time consumption for trend analysis
        int totalEverConsumed = transactions.stream()
            .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.STOCK_OUT)
            .mapToInt(StockTransaction::getQuantity).sum();

        // Predict days until stockout
        double daysUntilStockout = dailyConsumptionRate > 0
            ? medicine.getStockQuantity() / dailyConsumptionRate
            : 999.0;

        // Suggested reorder quantity (30-day supply + safety stock of 15 days)
        int suggestedReorderQty = (int) Math.ceil(dailyConsumptionRate * 45);

        // Urgency level
        String urgency;
        String urgencyColor;
        if (daysUntilStockout <= 3) { urgency = "CRITICAL"; urgencyColor = "#e74c3c"; }
        else if (daysUntilStockout <= 7) { urgency = "HIGH"; urgencyColor = "#f39c12"; }
        else if (daysUntilStockout <= 14) { urgency = "MEDIUM"; urgencyColor = "#3498db"; }
        else if (daysUntilStockout <= 30) { urgency = "LOW"; urgencyColor = "#27ae60"; }
        else { urgency = "SUFFICIENT"; urgencyColor = "#95a5a6"; }

        // Predicted stockout date
        LocalDate predictedStockoutDate = LocalDate.now().plusDays((long) daysUntilStockout);

        // Consumption trend (comparing last 7 days vs previous 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
        int last7Days = transactions.stream()
            .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.STOCK_OUT)
            .filter(t -> t.getTransactionDate() != null && t.getTransactionDate().isAfter(sevenDaysAgo))
            .mapToInt(StockTransaction::getQuantity).sum();
        int prev7Days = transactions.stream()
            .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.STOCK_OUT)
            .filter(t -> t.getTransactionDate() != null
                && t.getTransactionDate().isAfter(fourteenDaysAgo)
                && t.getTransactionDate().isBefore(sevenDaysAgo))
            .mapToInt(StockTransaction::getQuantity).sum();

        String trend;
        double trendPercent = 0;
        if (prev7Days > 0) {
            trendPercent = ((double)(last7Days - prev7Days) / prev7Days) * 100;
            trend = trendPercent > 10 ? "INCREASING" : (trendPercent < -10 ? "DECREASING" : "STABLE");
        } else {
            trend = "INSUFFICIENT_DATA";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("medicineId", medicine.getId());
        result.put("medicineName", medicine.getProductName());
        result.put("skuId", medicine.getSkuId());
        result.put("currentStock", medicine.getStockQuantity());
        result.put("dailyConsumptionRate", Math.round(dailyConsumptionRate * 100.0) / 100.0);
        result.put("daysUntilStockout", Math.round(daysUntilStockout * 10.0) / 10.0);
        result.put("predictedStockoutDate", predictedStockoutDate.toString());
        result.put("suggestedReorderQty", Math.max(suggestedReorderQty, 10));
        result.put("urgency", urgency);
        result.put("urgencyColor", urgencyColor);
        result.put("trend", trend);
        result.put("trendPercent", Math.round(trendPercent * 10.0) / 10.0);
        result.put("totalConsumed30Days", totalConsumed30Days);
        result.put("totalEverConsumed", totalEverConsumed);
        result.put("expiryDate", medicine.getExpiryDate() != null ? medicine.getExpiryDate().toString() : null);
        return result;
    }

    public Map<String, Object> getPredictionForMedicine(Long medicineId) {
        if (medicineId == null) {
            return null;
        }
        return medicineRepository.findById(medicineId)
            .map(this::analyzeMedicine)
            .orElse(null);
    }

    /**
     * Generate hourly demand heatmap data (0-23 hours x 7 days)
     */
    public Map<String, Object> generateDemandHeatmap() {
        List<StockTransaction> allOuts = stockTransactionRepository.findAll().stream()
            .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.STOCK_OUT)
            .filter(t -> t.getTransactionDate() != null)
            .collect(Collectors.toList());

        int[][] heatmap = new int[7][24]; // [dayOfWeek][hour]
        for (StockTransaction tx : allOuts) {
            int day = tx.getTransactionDate().getDayOfWeek().getValue() - 1; // 0=Mon
            int hour = tx.getTransactionDate().getHour();
            heatmap[day][hour] += tx.getQuantity();
        }

        int maxVal = 0;
        for (int[] row : heatmap) for (int v : row) if (v > maxVal) maxVal = v;

        Map<String, Object> result = new HashMap<>();
        result.put("heatmap", heatmap);
        result.put("maxValue", maxVal);
        result.put("days", new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"});
        result.put("totalTransactions", allOuts.size());
        return result;
    }
}
