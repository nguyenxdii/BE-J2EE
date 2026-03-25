package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.DepositWalletRequest;
import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.model.WalletTransaction;
import com.j2ee.carbooking.service.WalletService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // POST /api/wallet/deposit/momo — tạo payment URL (cần đăng nhập)
    @PostMapping("/deposit/momo")
    public ResponseEntity<AppApiResponse<String>> depositMomo(
            @RequestAttribute String userId,
            @Valid @RequestBody DepositWalletRequest request) throws Exception {

        String payUrl = walletService.createDeposit(userId, request);

        return ResponseEntity.ok(
            AppApiResponse.success("Tạo link thanh toán thành công", payUrl));
    }

    // POST /api/wallet/momo/callback — Momo gọi vào đây sau thanh toán
    @PostMapping("/momo/callback")
    public ResponseEntity<Void> momoCallback(@RequestParam Map<String, String> params) {
        walletService.handleMomoCallback(params);
        return ResponseEntity.ok().build();
    }

    // GET /api/wallet/balance — lấy số dư ví (cần đăng nhập)
    @GetMapping("/balance")
    public ResponseEntity<AppApiResponse<Double>> getBalance(
            @RequestAttribute String userId) {

        Double balance = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(AppApiResponse.success("Số dư tài khoản", balance));
    }

    // POST /api/wallet/confirm — Frontend gọi để cập nhật trạng thái ngay sau callback
    @PostMapping("/confirm")
    public ResponseEntity<AppApiResponse<Object>> confirmTransaction(
            @RequestBody Map<String, String> params) {
        
        walletService.handleMomoCallback(params);
        return ResponseEntity.ok(AppApiResponse.success("Đã cập nhật trạng thái giao dịch", null));
    }

    // GET /api/wallet/transactions — lịch sử giao dịch (cần đăng nhập)
    @GetMapping("/transactions")
    public ResponseEntity<AppApiResponse<List<WalletTransaction>>> getTransactions(
            @RequestAttribute String userId) {

        List<WalletTransaction> data =
            walletService.getTransactionHistory(userId);

        return ResponseEntity.ok(AppApiResponse.success("Lịch sử giao dịch", data));
    }
}
