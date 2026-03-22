// util/DataSeeder.java
package com.j2ee.carbooking.util;

import com.j2ee.carbooking.enums.*;
import com.j2ee.carbooking.model.*;
import com.j2ee.carbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final DepositListingRepository depositListingRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("🌱 Kiểm tra và nạp dữ liệu mẫu...");

        depositListingRepository.deleteAll();
        walletTransactionRepository.deleteAll();
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        seedUsers();
        seedCategories();
        seedVehicles();
        seedOrders();
        seedListings();

        System.out.println("✅ Hoàn tất quá trình làm sạch và nạp lại dữ liệu mẫu!");
    }

    // ==================== USERS ====================

    private void seedUsers() {
        // Admin
        User admin = new User();
        admin.setFullName("Admin ShopCar");
        admin.setEmail("admin@shopcar.com");
        admin.setPassword(passwordEncoder.encode("123qwe123"));
        admin.setPhone("0901234567");
        admin.setAvatar("https://i.pravatar.cc/150?img=1");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setWalletBalance(0.0);
        userRepository.save(admin);

        // User 1 — verified
        User user1 = new User();
        user1.setFullName("Nguyễn Văn An");
        user1.setEmail("an.nguyen@gmail.com");
        user1.setPassword(passwordEncoder.encode("123456"));
        user1.setPhone("0912345678");
        user1.setAvatar("https://i.pravatar.cc/150?img=2");
        user1.setRole(Role.USER);
        user1.setStatus(UserStatus.ACTIVE);
        user1.setWalletBalance(500000.0);
        user1.setIdentity(makeVerifiedIdentity());
        userRepository.save(user1);

        WalletTransaction tx1 = new WalletTransaction();
        tx1.setUserId(user1.getId());
        tx1.setType(TransactionType.DEPOSIT);
        tx1.setAmount(500000.0);
        tx1.setBalanceBefore(0.0);
        tx1.setBalanceAfter(500000.0);
        tx1.setRefType("WALLET");
        tx1.setRefId("MOCK-DEPOSIT-1");
        tx1.setDescription("Nạp tiền vào ví qua Momo (Dữ liệu mẫu)");
        tx1.setStatus(TransactionStatus.SUCCESS);
        tx1.setCreatedAt(java.time.LocalDateTime.now().minusHours(2));
        walletTransactionRepository.save(tx1);

        // User 2 — verified
        User user2 = new User();
        user2.setFullName("Trần Thị Bích");
        user2.setEmail("bich.tran@gmail.com");
        user2.setPassword(passwordEncoder.encode("123456"));
        user2.setPhone("0923456789");
        user2.setAvatar("https://i.pravatar.cc/150?img=5");
        user2.setRole(Role.USER);
        user2.setStatus(UserStatus.ACTIVE);
        user2.setWalletBalance(1200000.0);
        user2.setIdentity(makeVerifiedIdentity());
        userRepository.save(user2);

        // User 3 — chờ duyệt CCCD (để admin demo)
        User user3 = new User();
        user3.setFullName("Lê Minh Cường");
        user3.setEmail("cuong.le@gmail.com");
        user3.setPassword(passwordEncoder.encode("123456"));
        user3.setPhone("0934567890");
        user3.setAvatar("https://i.pravatar.cc/150?img=8");
        user3.setRole(Role.USER);
        user3.setStatus(UserStatus.ACTIVE);
        user3.setWalletBalance(0.0);
        Identity identity3 = new Identity();
        identity3.setCccdFront("https://placehold.co/400x250?text=CCCD+Mat+Truoc");
        identity3.setCccdBack("https://placehold.co/400x250?text=CCCD+Mat+Sau");
        identity3.setDrivingLicense("https://placehold.co/400x250?text=GPLX");
        identity3.setVerifyStatus(VerifyStatus.PENDING);
        user3.setIdentity(identity3);
        userRepository.save(user3);

        // User 4 — bị khoá (để admin demo)
        User user4 = new User();
        user4.setFullName("Phạm Thị Dung");
        user4.setEmail("dung.pham@gmail.com");
        user4.setPassword(passwordEncoder.encode("123456"));
        user4.setPhone("0945678901");
        user4.setAvatar("https://i.pravatar.cc/150?img=9");
        user4.setRole(Role.USER);
        user4.setStatus(UserStatus.LOCKED);
        user4.setWalletBalance(0.0);
        userRepository.save(user4);

        System.out.println("  ✔ Seeded 1 admin + 4 users");
    }

    private Identity makeVerifiedIdentity() {
        Identity i = new Identity();
        i.setCccdFront("https://placehold.co/400x250?text=CCCD+Mat+Truoc");
        i.setCccdBack("https://placehold.co/400x250?text=CCCD+Mat+Sau");
        i.setDrivingLicense("https://placehold.co/400x250?text=GPLX");
        i.setVerifyStatus(VerifyStatus.VERIFIED);
        return i;
    }

    // ==================== CATEGORIES ====================

    private void seedCategories() {
        categoryRepository.saveAll(List.of(
            makeCategory("Xe số",
                "Xe máy số tiết kiệm nhiên liệu, phù hợp đi phố",
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),
            makeCategory("Xe ga",
                "Xe tay ga tiện lợi, dễ lái cho mọi người",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=400"),
            makeCategory("Xe côn tay",
                "Xe côn tay mạnh mẽ cho người thích tốc độ",
                "https://images.unsplash.com/photo-1609630875171-b1321377ee65?w=400"),
            makeCategory("Xe điện",
                "Xe điện thân thiện môi trường, chi phí thấp",
                "https://images.unsplash.com/photo-1593014700857-a2c1d93cf63f?w=400")
        ));
        System.out.println("  ✔ Seeded 4 categories");
    }

    private Category makeCategory(String name, String description, String image) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        c.setImage(image);
        return c;
    }

    // ==================== VEHICLES ====================

    private void seedVehicles() {
        String catSo   = categoryRepository.findByName("Xe số").orElseThrow().getId();
        String catGa   = categoryRepository.findByName("Xe ga").orElseThrow().getId();
        String catCon  = categoryRepository.findByName("Xe côn tay").orElseThrow().getId();
        String catDien = categoryRepository.findByName("Xe điện").orElseThrow().getId();

        vehicleRepository.saveAll(List.of(

            // ---- XE SỐ ----
            makeVehicle("Honda Wave Alpha 2022", catSo, "Honda", "Wave Alpha", 2022,
                "51A-12345", 150000.0, 300000.0,
                "Xe số Honda Wave Alpha đời 2022, tiết kiệm xăng, bền bỉ, phù hợp đi lại hàng ngày.",
                List.of(
                    "https://muaxe.minhlongmoto.com/wp-content/uploads/2023/07/anh-xe-honda-wave-alpha-110-1-768x512.jpg",
                    "https://images2.thanhnien.vn/wave-alpha-2022.jpg"
                ),
                "110cc", "Xăng", "Số",
                VehicleStatus.AVAILABLE, 4.5, 12),

            makeVehicle("Honda Future 125 2023", catSo, "Honda", "Future 125", 2023,
                "51A-23456", 180000.0, 350000.0,
                "Future 125 FI đời 2023, động cơ phun xăng điện tử, vận hành êm và tiết kiệm.",
                List.of(
                    "https://cdnphoto.dantri.com.vn/thumb_w/960/2023/01/future-125.jpg"
                ),
                "125cc", "Xăng", "Số",
                VehicleStatus.AVAILABLE, 4.3, 8),

            makeVehicle("Yamaha Sirius 2022", catSo, "Yamaha", "Sirius", 2022,
                "51B-34567", 140000.0, 280000.0,
                "Sirius RC đời 2022, thiết kế trẻ trung, vận hành ổn định, thích hợp đường phố.",
                List.of(
                    "https://yamaha-motor.com.vn/wp-content/uploads/sirius-rc.jpg"
                ),
                "115cc", "Xăng", "Số",
                VehicleStatus.AVAILABLE, 4.2, 6),

            // ---- XE GA ----
            makeVehicle("Honda Air Blade 2023", catGa, "Honda", "Air Blade", 2023,
                "51B-45678", 220000.0, 500000.0,
                "Air Blade 125 đời 2023, thiết kế thể thao, cốp rộng, phanh ABS an toàn.",
                List.of(
                    "https://images2.thanhnien.vn/airblade-2023.jpg",
                    "https://cdnphoto.dantri.com.vn/airblade-125-abs.jpg"
                ),
                "125cc", "Xăng", "Tay ga",
                VehicleStatus.AVAILABLE, 4.8, 20),

            makeVehicle("Honda Vision 2023", catGa, "Honda", "Vision", 2023,
                "51C-56789", 190000.0, 400000.0,
                "Vision đời 2023, nhỏ gọn linh hoạt, tiết kiệm nhiên liệu, phù hợp cả nam và nữ.",
                List.of(
                    "https://images2.thanhnien.vn/honda-vision-2023.jpg",
                    "https://cdnphoto.dantri.com.vn/vision-2023.jpg"
                ),
                "110cc", "Xăng", "Tay ga",
                VehicleStatus.AVAILABLE, 4.6, 15),

            makeVehicle("Yamaha Freego 2022", catGa, "Yamaha", "Freego", 2022,
                "51C-67890", 200000.0, 450000.0,
                "Freego S ABS đời 2022, cốp chứa đồ lớn, kết nối Bluetooth, phong cách hiện đại.",
                List.of(
                    "https://yamaha-motor.com.vn/wp-content/uploads/freego-abs.jpg"
                ),
                "125cc", "Xăng", "Tay ga",
                VehicleStatus.AVAILABLE, 4.4, 9),

            makeVehicle("Yamaha Grande 2023", catGa, "Yamaha", "Grande", 2023,
                "51D-78901", 210000.0, 480000.0,
                "Grande Hybrid đời 2023, công nghệ hybrid tiết kiệm xăng đến 30%, sang trọng.",
                List.of(
                    "https://yamaha-motor.com.vn/wp-content/uploads/grande-hybrid-2023.jpg"
                ),
                "125cc", "Xăng", "Tay ga",
                VehicleStatus.AVAILABLE, 4.1, 5),

            // ---- XE CÔN TAY ----
            makeVehicle("Yamaha Exciter 155 2023", catCon, "Yamaha", "Exciter 155", 2023,
                "51D-89012", 280000.0, 600000.0,
                "Exciter 155 VVA đời 2023, động cơ 155cc mạnh mẽ, thiết kế thể thao cực ngầu.",
                List.of(
                    "https://images2.thanhnien.vn/exciter-155-2023.jpg",
                    "https://cdnphoto.dantri.com.vn/exciter155-do-den.jpg"
                ),
                "155cc", "Xăng", "Côn tay",
                VehicleStatus.AVAILABLE, 4.9, 25),

            makeVehicle("Honda Winner X 2022", catCon, "Honda", "Winner X", 2022,
                "51E-90123", 250000.0, 550000.0,
                "Winner X đời 2022, thiết kế racing, động cơ DOHC mạnh mẽ, phù hợp đường dài.",
                List.of(
                    "https://images2.thanhnien.vn/winner-x-2022.jpg",
                    "https://cdnphoto.dantri.com.vn/winner-x-trang.jpg"
                ),
                "150cc", "Xăng", "Côn tay",
                VehicleStatus.AVAILABLE, 4.7, 18),

            makeVehicle("Suzuki Raider R150 2022", catCon, "Suzuki", "Raider R150", 2022,
                "51E-01234", 240000.0, 520000.0,
                "Raider R150 đời 2022, động cơ fuel injection, thiết kế sport mạnh mẽ cá tính.",
                List.of(
                    "https://images.suzuki.com.vn/raider-r150-2022.jpg"
                ),
                "150cc", "Xăng", "Côn tay",
                VehicleStatus.AVAILABLE, 4.3, 7),

            // ---- XE ĐIỆN ----
            makeVehicle("VinFast Theon 2023", catDien, "VinFast", "Theon", 2023,
                "51F-12345", 160000.0, 350000.0,
                "VinFast Theon đời 2023, pin lithium bền, sạc nhanh, không tiếng ồn, thân thiện môi trường.",
                List.of(
                    "https://images.vinfastauto.com/theon-2023-xanh.jpg",
                    "https://images.vinfastauto.com/theon-2023-trang.jpg"
                ),
                "Electric", "Điện", "Tay ga",
                VehicleStatus.AVAILABLE, 4.2, 10),

            makeVehicle("VinFast Feliz S 2023", catDien, "VinFast", "Feliz S", 2023,
                "51F-23456", 150000.0, 320000.0,
                "Feliz S đời 2023, thiết kế trẻ trung, phạm vi di chuyển lên đến 200km mỗi lần sạc.",
                List.of(
                    "https://images.vinfastauto.com/feliz-s-2023.jpg"
                ),
                "Electric", "Điện", "Tay ga",
                VehicleStatus.AVAILABLE, 4.0, 4)
        ));

        System.out.println("  ✔ Seeded 12 vehicles");
    }

    private Vehicle makeVehicle(
            String name, String categoryId, String brand,
            String model, int year, String licensePlate,
            double pricePerDay, double depositAmount, String description,
            List<String> images, String engine, String fuelType,
            String transmission, VehicleStatus status,
            double avgRating, int totalReviews) {

        Specs specs = new Specs();
        specs.setEngine(engine);
        specs.setFuelType(fuelType);
        specs.setTransmission(transmission);

        Vehicle v = new Vehicle();
        v.setName(name);
        v.setCategoryId(categoryId);
        v.setBrand(brand);
        v.setModel(model);
        v.setYear(year);
        v.setLicensePlate(licensePlate);
        v.setPricePerDay(pricePerDay);
        v.setDepositAmount(depositAmount);
        v.setDescription(description);
        v.setImages(images);
        v.setSpecs(specs);
        v.setStatus(status);
        v.setAvgRating(avgRating);
        v.setTotalReviews(totalReviews);
        return v;
    }

    // ==================== ORDERS ====================

    private void seedOrders() {
        User an = userRepository.findByEmail("an.nguyen@gmail.com").orElseThrow();
        User bich = userRepository.findByEmail("bich.tran@gmail.com").orElseThrow();
        Vehicle vehicle1 = vehicleRepository.findAll().get(0);
        Vehicle vehicle2 = vehicleRepository.findAll().get(1);

        // 1. ĐƠN HÀNG HỢP LỆ (Để test Đăng bán thành công)
        Order o1 = createTestOrder("ORD-AN-OK-01", an.getId(), vehicle1, 5, 8, OrderStatus.CONFIRMED);
        
        // 2. ĐƠN HÀNG QUÁ HẠN (Để test lỗi > 24h)
        // Ngày mai bắt đầu rồi -> Chỉ còn < 24h -> Sẽ báo lỗi khi đăng bán
        Order o2 = createTestOrder("ORD-AN-EXPIRED", an.getId(), vehicle2, 1, 3, OrderStatus.CONFIRMED);

        // 3. ĐƠN HÀNG CỦA NGƯỜI KHÁC (Để test lỗi Không có quyền)
        Order o3 = createTestOrder("ORD-BICH-OK-01", bich.getId(), vehicle1, 10, 12, OrderStatus.CONFIRMED);

        orderRepository.saveAll(List.of(o1, o2, o3));

        System.out.println("  ✔ Đã nạp 3 đơn hàng mẫu để test:");
        System.out.println("    - Của An (Hợp lệ): " + o1.getId());
        System.out.println("    - Của An (Quá hạn 24h): " + o2.getId());
        System.out.println("    - Của Bích (Để test quyền): " + o3.getId());
    }

    private Order createTestOrder(String code, String userId, Vehicle vehicle, 
                                  int plusDaysStart, int plusDaysEnd, OrderStatus status) {
        Order order = new Order();
        order.setOrderCode(code);
        order.setUserId(userId);
        order.setVehicleId(vehicle.getId());
        order.setStartDate(LocalDate.now().plusDays(plusDaysStart));
        order.setEndDate(LocalDate.now().plusDays(plusDaysEnd));
        order.setTotalDays(plusDaysEnd - plusDaysStart);
        order.setRentalPrice(vehicle.getPricePerDay() * (plusDaysEnd - plusDaysStart));
        order.setDepositAmount(vehicle.getDepositAmount());
        order.setTotalAmount(order.getRentalPrice() + order.getDepositAmount());
        order.setStatus(status);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentMethod(PaymentMethod.WALLET);
        order.setIsTransferred(false);
        return order;
    }

    private void seedListings() {
        Order o1 = orderRepository.findByOrderCode("ORD-AN-OK-01").orElseThrow();
        Vehicle v1 = vehicleRepository.findById(o1.getVehicleId()).orElseThrow();

        DepositListing listing = new DepositListing();
        listing.setSellerId(o1.getUserId());
        listing.setOrderId(o1.getId());
        listing.setVehicleId(o1.getVehicleId());
        listing.setOriginalDeposit(o1.getDepositAmount());
        listing.setSellingPrice(o1.getDepositAmount() * 0.6); // 60%
        listing.setPlatformFee(o1.getDepositAmount() * 0.4);
        listing.setExpiredAt(o1.getStartDate().atStartOfDay().minusHours(24));
        listing.setStatus(DepositListingStatus.OPEN);

        depositListingRepository.save(listing);

        // --- TẠO BÀI ĐĂNG ĐÃ HẾT HẠN (Để test Scheduler) ---
        Order o2 = orderRepository.findByOrderCode("ORD-AN-EXPIRED").orElseThrow();
        DepositListing expiredListing = new DepositListing();
        expiredListing.setSellerId(o2.getUserId());
        expiredListing.setOrderId(o2.getId());
        expiredListing.setVehicleId(o2.getVehicleId());
        expiredListing.setOriginalDeposit(o2.getDepositAmount());
        expiredListing.setSellingPrice(o2.getDepositAmount() * 0.6);
        expiredListing.setPlatformFee(o2.getDepositAmount() * 0.4);
        // Đặt hết hạn vào 1 giờ trước
        expiredListing.setExpiredAt(LocalDateTime.now().minusHours(1));
        expiredListing.setStatus(DepositListingStatus.OPEN);

        depositListingRepository.save(expiredListing);

        System.out.println("  ✔ Đã tạo 1 bài đăng suất cọc mẫu:");
        System.out.println("    - Của An (OPEN): " + listing.getId() + " cho xe " + v1.getName());
        System.out.println("    - Của An (Hết hạn - Chờ quét): " + expiredListing.getId());
    }
}