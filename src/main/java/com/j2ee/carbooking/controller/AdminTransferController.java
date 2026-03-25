package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.enums.DepositListingStatus;
import com.j2ee.carbooking.model.DepositListing;
import com.j2ee.carbooking.repository.DepositListingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/transfers")
public class AdminTransferController {

    private final DepositListingRepository depositListingRepository;

    public AdminTransferController(DepositListingRepository depositListingRepository) {
        this.depositListingRepository = depositListingRepository;
    }

    @GetMapping("/history")
    public ResponseEntity<AppApiResponse<Map<String, Object>>> getTransferHistory() {
        List<DepositListing> soldListings = depositListingRepository.findAll().stream()
            .filter(l -> l.getStatus() == DepositListingStatus.SOLD)
            .collect(Collectors.toList());

        double totalFees = soldListings.stream().mapToDouble(DepositListing::getPlatformFee).sum();

        Map<String, Object> response = new HashMap<>();
        response.put("transfers", soldListings);
        response.put("totalPlatformFees", totalFees);

        return ResponseEntity.ok(AppApiResponse.success("Lấy lịch sử sang nhượng thành công", response));
    }
}
