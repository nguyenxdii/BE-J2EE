package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.dto.response.TransactionResponse;
import com.j2ee.carbooking.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transactions")
public class AdminTransactionController {

    private final WalletService walletService;

    public AdminTransactionController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/all")
    public ResponseEntity<AppApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = walletService.getAllTransactions();
        return ResponseEntity.ok(AppApiResponse.success("Lấy toàn bộ giao dịch thành công", transactions));
    }
}
