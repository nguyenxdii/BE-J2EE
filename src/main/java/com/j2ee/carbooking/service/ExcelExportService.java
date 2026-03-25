package com.j2ee.carbooking.service;

import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.repository.OrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    private final OrderRepository orderRepository;

    public ExcelExportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public ByteArrayInputStream exportOrders() {
        List<Order> orders = orderRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Orders");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã đơn", "User ID", "Xe ID", "Ngày bắt đầu", "Ngày kết thúc", "Tổng ngày", "Tiền thuê", "Tiền cọc", "Tổng tiền", "Trạng thái"};
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data
            int rowIdx = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getOrderCode());
                row.createCell(2).setCellValue(order.getUserId());
                row.createCell(3).setCellValue(order.getVehicleId());
                row.createCell(4).setCellValue(order.getStartDate().toString());
                row.createCell(5).setCellValue(order.getEndDate().toString());
                row.createCell(6).setCellValue(order.getTotalDays());
                row.createCell(7).setCellValue(order.getRentalPrice());
                row.createCell(8).setCellValue(order.getDepositAmount());
                row.createCell(9).setCellValue(order.getTotalAmount());
                row.createCell(10).setCellValue(order.getStatus().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }
}
