package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.dto.response.BuyDepositListingResponse;
import com.j2ee.carbooking.dto.response.DepositListingResponse;
import com.j2ee.carbooking.service.DepositListingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
public class DepositListingController {

    private final DepositListingService depositListingService;

    public DepositListingController(DepositListingService depositListingService) {
        this.depositListingService = depositListingService;
    }

    @PostMapping("/listings")
    public ResponseEntity<AppApiResponse<DepositListingResponse>> createListing(
            @RequestAttribute String userId,
            @Valid @RequestBody CreateDepositListingRequest request) {

        DepositListingResponse data =
            depositListingService.createListing(userId, request);

        return ResponseEntity.ok(
            AppApiResponse.success("Đăng bán suất cọc thành công", data));
    }

    @GetMapping("/listings")
    public ResponseEntity<AppApiResponse<List<DepositListingResponse>>> getOpenListings() {

        List<DepositListingResponse> data =
            depositListingService.getOpenListings();

        return ResponseEntity.ok(
            AppApiResponse.success("Danh sách suất cọc", data));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<AppApiResponse<Void>> cancelListing(
            @RequestAttribute String userId,
            @PathVariable String id) {

        depositListingService.cancelListing(userId, id);

        return ResponseEntity.ok(
            AppApiResponse.success("Đã xoá bài đăng suất cọc"));
    }

    @PostMapping("/buy")
    public ResponseEntity<AppApiResponse<BuyDepositListingResponse>> buyListing(
            @RequestAttribute String userId,
            @Valid @RequestBody BuyDepositListingRequest request) throws Exception {

        BuyDepositListingResponse data =
            depositListingService.buyListing(userId, request);

        return ResponseEntity.ok(
            AppApiResponse.success("Xử lý mua suất cọc thành công", data));
    }
}
