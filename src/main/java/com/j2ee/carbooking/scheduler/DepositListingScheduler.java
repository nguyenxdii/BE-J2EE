package com.j2ee.carbooking.scheduler;

import com.j2ee.carbooking.enums.DepositListingStatus;
import com.j2ee.carbooking.model.DepositListing;
import com.j2ee.carbooking.repository.DepositListingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DepositListingScheduler {

    private final DepositListingRepository depositListingRepository;

    public DepositListingScheduler(DepositListingRepository depositListingRepository) {
        this.depositListingRepository = depositListingRepository;
    }

    @Scheduled(fixedRate = 3600000)
    public void closeExpiredListings() {
        List<DepositListing> expired = depositListingRepository
            .findByStatusAndExpiredAtBefore(
                DepositListingStatus.OPEN,
                LocalDateTime.now()
            );

        if (expired.isEmpty()) return;

        expired.forEach(listing ->
            listing.setStatus(DepositListingStatus.EXPIRED));

        depositListingRepository.saveAll(expired);

        System.out.println("Scheduler: Đã đóng "
            + expired.size() + " suất cọc hết hạn lúc "
            + LocalDateTime.now());
    }
}
