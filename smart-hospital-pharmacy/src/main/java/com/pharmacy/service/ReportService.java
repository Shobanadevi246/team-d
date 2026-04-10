package com.pharmacy.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.pharmacy.entity.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MedicineService medicineService;
    private final StaffService staffService;
    private final FeedbackService feedbackService;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===================== PDF REPORTS =====================

    public byte[] generateMedicinePdfReport() throws Exception {
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addPdfTitle(doc, "Medicine Report", "Smart Hospital Pharmacy System");

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 2f, 1.5f, 2f, 2f});

        addPdfHeaders(table, "S.No", "Product Name", "SKU ID", "Qty", "Expiry Date", "Added By");

        List<Medicine> medicines = medicineService.getAllMedicines();
        int i = 1;
        for (Medicine m : medicines) {
            addPdfCell(table, String.valueOf(i++));
            addPdfCell(table, m.getProductName());
            addPdfCell(table, m.getSkuId());
            addPdfCell(table, String.valueOf(m.getStockQuantity()));
            addPdfCell(table, m.getExpiryDate() != null ? m.getExpiryDate().format(DF) : "N/A");
            addPdfCell(table, m.isAdminAdded() ? "Admin" :
                    (m.getAddedByStaff() != null ? m.getAddedByStaff().getStaffName() : "N/A"));
        }
        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateStaffPdfReport() throws Exception {
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addPdfTitle(doc, "Staff Report", "Smart Hospital Pharmacy System");

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 2f, 3f, 2f, 2f});

        addPdfHeaders(table, "S.No", "Staff Name", "Staff ID", "Email", "Shop No", "Status");

        List<Staff> staffList = staffService.getAllStaff();
        int i = 1;
        for (Staff s : staffList) {
            addPdfCell(table, String.valueOf(i++));
            addPdfCell(table, s.getStaffName());
            addPdfCell(table, s.getStaffId());
            addPdfCell(table, s.getEmail());
            addPdfCell(table, s.getShopNumber() + " - " + s.getShopBlockName());
            addPdfCell(table, s.isLocked() ? "LOCKED" : (s.isEnabled() ? "ACTIVE" : "DISABLED"));
        }
        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateFeedbackPdfReport() throws Exception {
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addPdfTitle(doc, "Customer Feedback Report", "Smart Hospital Pharmacy System");

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2.5f, 2f, 2f, 2f, 3f});

        addPdfHeaders(table, "S.No", "Customer", "Type", "Medicine", "Brand", "Review");

        List<CustomerFeedback> feedbacks = feedbackService.getAllFeedback();
        int i = 1;
        for (CustomerFeedback f : feedbacks) {
            addPdfCell(table, String.valueOf(i++));
            addPdfCell(table, f.getCustomerName());
            addPdfCell(table, f.getCustomerType().name());
            addPdfCell(table, f.getMedicineName());
            addPdfCell(table, f.getMedicineBrand());
            addPdfCell(table, f.getReview().length() > 60 ? f.getReview().substring(0, 60) + "..." : f.getReview());
        }
        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    public byte[] generateAllDataPdfReport() throws Exception {
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        addPdfTitle(doc, "Complete System Report", "Smart Hospital Pharmacy System");

        // Medicines section
        doc.add(new Paragraph("\n"));
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(26, 58, 92));
        doc.add(new Paragraph("MEDICINE INVENTORY", sectionFont));
        doc.add(new Paragraph("\n"));

        PdfPTable mTable = new PdfPTable(5);
        mTable.setWidthPercentage(100);
        addPdfHeaders(mTable, "Product Name", "SKU", "Qty", "Expiry", "Added By");
        for (Medicine m : medicineService.getAllMedicines()) {
            addPdfCell(mTable, m.getProductName());
            addPdfCell(mTable, m.getSkuId());
            addPdfCell(mTable, String.valueOf(m.getStockQuantity()));
            addPdfCell(mTable, m.getExpiryDate() != null ? m.getExpiryDate().format(DF) : "N/A");
            addPdfCell(mTable, m.isAdminAdded() ? "Admin" :
                    (m.getAddedByStaff() != null ? m.getAddedByStaff().getStaffName() : "N/A"));
        }
        doc.add(mTable);

        // Staff section
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("STAFF DETAILS", sectionFont));
        doc.add(new Paragraph("\n"));

        PdfPTable sTable = new PdfPTable(5);
        sTable.setWidthPercentage(100);
        addPdfHeaders(sTable, "Staff Name", "Staff ID", "Email", "Shop", "Status");
        for (Staff s : staffService.getAllStaff()) {
            addPdfCell(sTable, s.getStaffName());
            addPdfCell(sTable, s.getStaffId());
            addPdfCell(sTable, s.getEmail());
            addPdfCell(sTable, s.getShopNumber());
            addPdfCell(sTable, s.isLocked() ? "LOCKED" : "ACTIVE");
        }
        doc.add(sTable);

        doc.close();
        return out.toByteArray();
    }

    // ===================== EXCEL REPORTS =====================

    public byte[] generateMedicineExcelReport() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Medicine Report");

        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle dataStyle = createDataStyle(wb);
        CellStyle titleStyle = createTitleStyle(wb);

        // Title row
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Smart Hospital Pharmacy - Medicine Report");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        // Headers
        Row headerRow = sheet.createRow(2);
        String[] headers = {"S.No", "Product Name", "SKU ID", "Quantity", "Expiry Date", "Added By"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        List<Medicine> medicines = medicineService.getAllMedicines();
        int rowNum = 3;
        for (int i = 0; i < medicines.size(); i++) {
            Medicine m = medicines.get(i);
            Row row = sheet.createRow(rowNum++);
            createDataCell(row, 0, String.valueOf(i + 1), dataStyle);
            createDataCell(row, 1, m.getProductName(), dataStyle);
            createDataCell(row, 2, m.getSkuId(), dataStyle);
            createDataCell(row, 3, String.valueOf(m.getStockQuantity()), dataStyle);
            createDataCell(row, 4, m.getExpiryDate() != null ? m.getExpiryDate().format(DF) : "N/A", dataStyle);
            createDataCell(row, 5, m.isAdminAdded() ? "Admin" :
                    (m.getAddedByStaff() != null ? m.getAddedByStaff().getStaffName() : "N/A"), dataStyle);
        }

        for (int i = 0; i < 6; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    public byte[] generateStaffExcelReport() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Staff Report");

        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle dataStyle = createDataStyle(wb);
        CellStyle titleStyle = createTitleStyle(wb);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Smart Hospital Pharmacy - Staff Report");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        Row headerRow = sheet.createRow(2);
        String[] headers = {"S.No", "Staff Name", "Staff ID", "Email", "Shop No", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Staff> staffList = staffService.getAllStaff();
        int rowNum = 3;
        for (int i = 0; i < staffList.size(); i++) {
            Staff s = staffList.get(i);
            Row row = sheet.createRow(rowNum++);
            createDataCell(row, 0, String.valueOf(i + 1), dataStyle);
            createDataCell(row, 1, s.getStaffName(), dataStyle);
            createDataCell(row, 2, s.getStaffId(), dataStyle);
            createDataCell(row, 3, s.getEmail(), dataStyle);
            createDataCell(row, 4, s.getShopNumber() + " - " + s.getShopBlockName(), dataStyle);
            createDataCell(row, 5, s.isLocked() ? "LOCKED" : "ACTIVE", dataStyle);
        }

        for (int i = 0; i < 6; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    public byte[] generateFeedbackExcelReport() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Feedback Report");

        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle dataStyle = createDataStyle(wb);
        CellStyle titleStyle = createTitleStyle(wb);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Smart Hospital Pharmacy - Customer Feedback Report");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        Row headerRow = sheet.createRow(2);
        String[] headers = {"S.No", "Customer Name", "Type", "Medicine", "Brand", "Review"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<CustomerFeedback> feedbacks = feedbackService.getAllFeedback();
        int rowNum = 3;
        for (int i = 0; i < feedbacks.size(); i++) {
            CustomerFeedback f = feedbacks.get(i);
            Row row = sheet.createRow(rowNum++);
            createDataCell(row, 0, String.valueOf(i + 1), dataStyle);
            createDataCell(row, 1, f.getCustomerName(), dataStyle);
            createDataCell(row, 2, f.getCustomerType().name(), dataStyle);
            createDataCell(row, 3, f.getMedicineName(), dataStyle);
            createDataCell(row, 4, f.getMedicineBrand(), dataStyle);
            createDataCell(row, 5, f.getReview(), dataStyle);
        }

        for (int i = 0; i < 6; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    public byte[] generateAllDataExcelReport() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle dataStyle = createDataStyle(wb);
        CellStyle titleStyle = createTitleStyle(wb);

        // Medicine Sheet
        XSSFSheet ms = wb.createSheet("Medicines");
        Cell mc = ms.createRow(0).createCell(0);
        mc.setCellValue("Smart Hospital Pharmacy - Complete Medicine Report");
        mc.setCellStyle(titleStyle);
        ms.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        Row mhr = ms.createRow(2);
        String[] mh = {"S.No","Product Name","SKU ID","Quantity","Expiry Date","Added By"};
        for (int i = 0; i < mh.length; i++) { Cell c = mhr.createCell(i); c.setCellValue(mh[i]); c.setCellStyle(headerStyle); }
        List<Medicine> medicines = medicineService.getAllMedicines();
        int rn = 3;
        for (int i = 0; i < medicines.size(); i++) {
            Medicine m = medicines.get(i); Row r = ms.createRow(rn++);
            createDataCell(r, 0, String.valueOf(i+1), dataStyle); createDataCell(r, 1, m.getProductName(), dataStyle);
            createDataCell(r, 2, m.getSkuId(), dataStyle); createDataCell(r, 3, String.valueOf(m.getStockQuantity()), dataStyle);
            createDataCell(r, 4, m.getExpiryDate() != null ? m.getExpiryDate().format(DF) : "N/A", dataStyle);
            createDataCell(r, 5, m.isAdminAdded() ? "Admin" : (m.getAddedByStaff() != null ? m.getAddedByStaff().getStaffName() : "N/A"), dataStyle);
        }
        for (int i = 0; i < 6; i++) ms.autoSizeColumn(i);

        // Staff Sheet
        XSSFSheet ss = wb.createSheet("Staff");
        Cell sc = ss.createRow(0).createCell(0);
        sc.setCellValue("Smart Hospital Pharmacy - Staff Report");
        sc.setCellStyle(titleStyle);
        ss.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        Row shr = ss.createRow(2);
        String[] sh = {"S.No","Staff Name","Staff ID","Email","Shop","Status"};
        for (int i = 0; i < sh.length; i++) { Cell c = shr.createCell(i); c.setCellValue(sh[i]); c.setCellStyle(headerStyle); }
        List<Staff> staffList = staffService.getAllStaff();
        int srn = 3;
        for (int i = 0; i < staffList.size(); i++) {
            Staff s = staffList.get(i); Row r = ss.createRow(srn++);
            createDataCell(r, 0, String.valueOf(i+1), dataStyle); createDataCell(r, 1, s.getStaffName(), dataStyle);
            createDataCell(r, 2, s.getStaffId(), dataStyle); createDataCell(r, 3, s.getEmail(), dataStyle);
            createDataCell(r, 4, s.getShopNumber() + " - " + s.getShopBlockName(), dataStyle);
            createDataCell(r, 5, s.isLocked() ? "LOCKED" : "ACTIVE", dataStyle);
        }
        for (int i = 0; i < 6; i++) ss.autoSizeColumn(i);

        // Feedback Sheet
        XSSFSheet fs = wb.createSheet("Feedback");
        Cell fc = fs.createRow(0).createCell(0);
        fc.setCellValue("Smart Hospital Pharmacy - Customer Feedback");
        fc.setCellStyle(titleStyle);
        fs.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        Row fhr = fs.createRow(2);
        String[] fh = {"S.No","Customer Name","Type","Medicine","Brand","Review"};
        for (int i = 0; i < fh.length; i++) { Cell c = fhr.createCell(i); c.setCellValue(fh[i]); c.setCellStyle(headerStyle); }
        List<CustomerFeedback> feedbacks = feedbackService.getAllFeedback();
        int frn = 3;
        for (int i = 0; i < feedbacks.size(); i++) {
            CustomerFeedback f = feedbacks.get(i); Row r = fs.createRow(frn++);
            createDataCell(r, 0, String.valueOf(i+1), dataStyle); createDataCell(r, 1, f.getCustomerName(), dataStyle);
            createDataCell(r, 2, f.getCustomerType().name(), dataStyle); createDataCell(r, 3, f.getMedicineName(), dataStyle);
            createDataCell(r, 4, f.getMedicineBrand(), dataStyle); createDataCell(r, 5, f.getReview(), dataStyle);
        }
        for (int i = 0; i < 6; i++) fs.autoSizeColumn(i);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out); wb.close();
        return out.toByteArray();
    }

    // ===================== HELPERS =====================

    private void addPdfTitle(Document doc, String title, String subtitle) throws Exception {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(26, 58, 92));
        Font subFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.GRAY);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        Paragraph subPara = new Paragraph(subtitle, subFont);
        subPara.setAlignment(Element.ALIGN_CENTER);
        doc.add(titlePara);
        doc.add(subPara);
        doc.add(new Paragraph("\n"));
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(new BaseColor(13, 124, 102));
        doc.add(new Chunk(ls));
        doc.add(new Paragraph("\n"));
    }

    private void addPdfHeaders(PdfPTable table, String... headers) {
        Font hFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
            cell.setBackgroundColor(new BaseColor(26, 58, 92));
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addPdfCell(PdfPTable table, String value) {
        Font f = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", f));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new byte[]{26, 58, 92}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createTitleStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(new XSSFColor(new byte[]{26, 58, 92}, null));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createDataCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
}
