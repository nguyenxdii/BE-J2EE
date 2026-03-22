package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.*;
import com.j2ee.carbooking.service.DepositListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositListingController {

    private final DepositListingService depositListingService;

    // POST /api/deposits/listings — đăng bán suất cọc (cần đăng nhập)
    @PostMapping("/listings")
    public ResponseEntity<ApiResponse<DepositListingResponse>> createListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateDepositListingRequest request) {

        DepositListingResponse data =
            depositListingService.createListing(userDetails.getUsername(), request);

        return ResponseEntity.ok(
            ApiResponse.success("Đăng bán suất cọc thành công", data));
    }

    // GET /api/deposits/listings — xem marketplace (không cần đăng nhập)
    @GetMapping("/listings")
    public ResponseEntity<ApiResponse<List<DepositListingResponse>>> getOpenListings() {

        List<DepositListingResponse> data =
            depositListingService.getOpenListings();

        return ResponseEntity.ok(
            ApiResponse.success("Danh sách suất cọc", data));
    }

    // DELETE /api/deposits/listings/{id} — xoá bài đăng (cần đăng nhập)
    @DeleteMapping("/listings/{id}")
    public ResponseEntity<ApiResponse<?>> cancelListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        depositListingService.cancelListing(userDetails.getUsername(), id);

        return ResponseEntity.ok(
            ApiResponse.success("Đã xoá bài đăng suất cọc"));
    }

    // POST /api/deposits/buy — mua suất cọc (cần đăng nhập)
    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<BuyDepositListingResponse>> buyListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BuyDepositListingRequest request) throws Exception {

        BuyDepositListingResponse data =
            depositListingService.buyListing(userDetails.getUsername(), request);

        return ResponseEntity.ok(
            ApiResponse.success("Xử lý mua suất cọc thành công", data));
    }
}
