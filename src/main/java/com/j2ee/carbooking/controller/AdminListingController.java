package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.enums.DepositListingStatus;
import com.j2ee.carbooking.enums.NotificationType;
import com.j2ee.carbooking.model.DepositListing;
import com.j2ee.carbooking.repository.DepositListingRepository;
import com.j2ee.carbooking.service.MailService;
import com.j2ee.carbooking.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/deposit-listings")
public class AdminListingController {

    private final DepositListingRepository depositListingRepository;
    private final NotificationService notificationService;
    private final MailService mailService;

    public AdminListingController(DepositListingRepository depositListingRepository, NotificationService notificationService, MailService mailService) {
        this.depositListingRepository = depositListingRepository;
        this.notificationService = notificationService;
        this.mailService = mailService;
    }

    @GetMapping
    public ResponseEntity<AppApiResponse<List<DepositListing>>> getAllListings(@RequestParam(required = false) DepositListingStatus status) {
        List<DepositListing> listings = (status != null) 
            ? depositListingRepository.findAll().stream().filter(l -> l.getStatus() == status).toList()
            : depositListingRepository.findAll();
        return ResponseEntity.ok(AppApiResponse.success("Lấy danh sách bài đăng thành công", listings));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppApiResponse<Void>> cancelListing(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        
        String reason = body.getOrDefault("reason", "Vi phạm quy định nền tảng");
        DepositListing listing = depositListingRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));
        
        listing.setStatus(DepositListingStatus.CANCELLED);
        depositListingRepository.save(listing);

        // Thông báo cho người bán
        notificationService.create(listing.getSellerId(), "Bài đăng bị gỡ", 
            "Bài đăng suất cọc của bạn đã bị gỡ. Lý do: " + reason, NotificationType.SYSTEM, null);
        
        mailService.sendEmail(listing.getSellerId(), "Thông báo gỡ bài đăng", 
            "Chào bạn,\n\nBài đăng suất cọc của bạn đã bị Admin gỡ bỏ vì lý do: " + reason + ".\n\nTrân trọng!");

        return ResponseEntity.ok(AppApiResponse.success("Đã gỡ bài đăng thành công"));
    }
}
