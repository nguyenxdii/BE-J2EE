package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.DepositWalletRequest;
import com.j2ee.carbooking.dto.response.ApiResponse;
import com.j2ee.carbooking.model.WalletTransaction;
import com.j2ee.carbooking.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // POST /api/wallet/deposit/momo — tạo payment URL (cần đăng nhập)
    @PostMapping("/deposit/momo")
    public ResponseEntity<ApiResponse<String>> depositMomo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositWalletRequest request) throws Exception {

        String payUrl = walletService.depositViaMomo(
            userDetails.getUsername(), request);

        return ResponseEntity.ok(
            ApiResponse.success("Tạo link thanh toán thành công", payUrl));
    }

    // POST /api/wallet/momo/callback — Momo gọi vào đây sau thanh toán
    @PostMapping("/momo/callback")
    public ResponseEntity<String> momoCallback(
            @RequestBody Map<String, String> params) {

        walletService.handleMomoCallback(params);
        return ResponseEntity.ok("OK");
    }

    // GET /api/wallet/transactions — lịch sử giao dịch (cần đăng nhập)
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WalletTransaction> data =
            walletService.getTransactionHistory(userDetails.getUsername());

        return ResponseEntity.ok(
            ApiResponse.success("Lịch sử giao dịch", data));
    }
}
