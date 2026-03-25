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
        System.out.println("🌱 Kiểm tra và nạp dữ liệu mẫu (Robust Seeding)...");

        seedUsers();
        seedData();
        seedOrders();
        seedListings();

        System.out.println("✅ Hoàn tất kiểm tra dữ liệu mẫu!");
    }

    private void seedUsers() {
        if (userRepository.findByEmail("admin@shopcar.com").isEmpty()) {
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
        }

        userRepository.findByEmail("an.nguyen@gmail.com").ifPresentOrElse(user -> {
            user.setIdentity(makeVerifiedIdentity());
            userRepository.save(user);
        }, () -> {
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
        });

        userRepository.findByEmail("bich.tran@gmail.com").ifPresentOrElse(user -> {
            user.setIdentity(makeVerifiedIdentity());
            userRepository.save(user);
        }, () -> {
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
        });

        if (userRepository.findByEmail("cuong.le@gmail.com").isEmpty()) {
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
        }

        // Thêm 3 user theo yêu cầu mới (nếu chưa có)
        seedNewTestUser("user1@gmail.com", "User Một", true);
        seedNewTestUser("user2@gmail.com", "User Hai", true);
        seedNewTestUser("user3@gmail.com", "User Ba", false);

        System.out.println("  ✔ Đã kiểm tra và nạp User");
    }

    private void seedNewTestUser(String email, String name, boolean isVerified) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        
        if (user.getId() == null) {
            user.setFullName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("123qwe123"));
            user.setPhone("0999999999");
            user.setRole(Role.USER);
            user.setStatus(UserStatus.ACTIVE);
        }
        
        if (isVerified) {
            user.setIdentity(makeVerifiedIdentity());
        } else if (user.getIdentity() == null) {
            Identity i = new Identity();
            i.setCccdFront("https://placehold.co/400x250?text=CCCD+Mat+Truoc");
            i.setCccdBack("https://placehold.co/400x250?text=CCCD+Mat+Sau");
            i.setDrivingLicense("https://placehold.co/400x250?text=GPLX");
            i.setVerifyStatus(VerifyStatus.PENDING);
            user.setIdentity(i);
        }
        userRepository.save(user);
    }

    private Identity makeVerifiedIdentity() {
        Identity i = new Identity();
        i.setCccdFront("https://placehold.co/400x250?text=CCCD+Mat+Truoc");
        i.setCccdBack("https://placehold.co/400x250?text=CCCD+Mat+Sau");
        i.setDrivingLicense("https://placehold.co/400x250?text=GPLX");
        i.setVerifyStatus(VerifyStatus.VERIFIED);
        return i;
    }

    private void seedData() {
        // Cấu trúc: Lấy ID hoặc tạo mới, sau đó dùng ID đó cho bước tiếp theo
        String catSo = getOrCreateCategory("Xe số",
                "Xe máy số tiết kiệm nhiên liệu, phù hợp đi phố",
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400");
        String catGa = getOrCreateCategory("Xe ga",
                "Xe tay ga tiện lợi, dễ lái cho mọi người",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=400");
        String catCon = getOrCreateCategory("Xe côn tay",
                "Xe côn tay mạnh mẽ cho người thích tốc độ",
                "https://images.unsplash.com/photo-1609630875171-b1321377ee65?w=400");
        String catDien = getOrCreateCategory("Xe điện",
                "Xe điện thân thiện môi trường, chi phí thấp",
                "https://images.unsplash.com/photo-1593014700857-a2c1d93cf63f?w=400");

        System.out.println("  ✔ Đã kiểm tra Categories");

        // Seed Vehicles only if empty (optional, or check by license plate)
        if (vehicleRepository.count() == 0) {
            seedVehicles(catSo, catGa, catCon, catDien);
        }
    }

    private String getOrCreateCategory(String name, String desc, String img) {
        // Sử dụng stream().filter() để tránh lỗi NonUniqueResult nếu trước đó lỡ tạo trùng
        return categoryRepository.findAll().stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Category::getId)
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name);
                    c.setDescription(desc);
                    c.setImage(img);
                    Category saved = categoryRepository.save(c);
                    return saved.getId();
                });
    }

    private void seedVehicles(String catSo, String catGa, String catCon, String catDien) {
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
                    "https://hondagiapbinhduong.com/wp-content/uploads/2021/10/Xanh-den-dac-biet.jpg"
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
                    "https://xemaynhapkhau.com/wp-content/uploads/2023/03/125_db_denvang.jpg",
                    "https://xemaynhapkhau.com/wp-content/uploads/2023/03/125_db_denvang.jpg"
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
                    "https://yamaha-motor.com.vn/wp/wp-content/uploads/2019/04/FreeGo-S-Mat-Grey-004.png"
                ),
                "125cc", "Xăng", "Tay ga",
                VehicleStatus.AVAILABLE, 4.4, 9),

            makeVehicle("Yamaha Grande 2023", catGa, "Yamaha", "Grande", 2023,
                "51D-78901", 210000.0, 480000.0,
                "Grande Hybrid đời 2023, công nghệ hybrid tiết kiệm xăng đến 30%, sang trọng.",
                List.of(
                    "https://xemayyamahanamtien.com/img/upload/images/2023/9/yamaha-grande-2023-dac-biet-mau-xanh-mau-den-mau-do-23991156-0.png"
                ),
                "125cc", "Xăng", "Tay ga",
                VehicleStatus.AVAILABLE, 4.1, 5),

            // ---- XE CÔN TAY ----
            makeVehicle("Yamaha Exciter 155 2023", catCon, "Yamaha", "Exciter 155", 2023,
                "51D-89012", 280000.0, 600000.0,
                "Exciter 155 VVA đời 2023, động cơ 155cc mạnh mẽ, thiết kế thể thao cực ngầu.",
                List.of(
                    "https://media.vov.vn/sites/default/files/styles/large/public/2023-09/exciter_155_vva_gp_abs_004.png.jpg"
                ),
                "155cc", "Xăng", "Côn tay",
                VehicleStatus.AVAILABLE, 4.9, 25),

            makeVehicle("Honda Winner X 2022", catCon, "Honda", "Winner X", 2022,
                "51E-90123", 250000.0, 550000.0,
                "Winner X đời 2022, thiết kế racing, động cơ DOHC mạnh mẽ, phù hợp đường dài.",
                List.of(
                    "https://media.vov.vn/sites/default/files/styles/large/public/2021-12/phien_ban_the_thao_xe_do_den.jpg"
                ),
                "150cc", "Xăng", "Côn tay",
                VehicleStatus.AVAILABLE, 4.7, 18),



            // ---- XE ĐIỆN ----
            makeVehicle("VinFast Theon 2023", catDien, "VinFast", "Theon", 2023,
                "51F-12345", 160000.0, 350000.0,
                "VinFast Theon đời 2023, pin lithium bền, sạc nhanh, không tiếng ồn, thân thiện môi trường.",
                List.of(
                    "https://i2-vnexpress.vnecdn.net/2023/04/07/vinfasttheons4jpg-1680855077.png?w=1200&h=0&q=100&dpr=1&fit=crop&s=7s26DUPaYakhyrZDNatt-Q"
                ),
                "Electric", "Điện", "Tay ga",
                VehicleStatus.AVAILABLE, 4.2, 10),

            makeVehicle("VinFast Feliz S 2023", catDien, "VinFast", "Feliz S", 2023,
                "51F-23456", 150000.0, 320000.0,
                "Feliz S đời 2023, thiết kế trẻ trung, phạm vi di chuyển lên đến 200km mỗi lần sạc.",
                List.of(
                    "https://i2-vnexpress.vnecdn.net/2023/04/07/vinfastfelizs3jpg-1680854039.png?w=1200&h=0&q=100&dpr=1&fit=crop&s=yCF1KrWC3wBRrHlRxokuHA"
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
        if (orderRepository.count() > 0) return;
        
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

        System.out.println("  ✔ Đã nạp 3 đơn hàng mẫu");
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
        if (depositListingRepository.count() > 0) return;

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

        System.out.println("  ✔ Đã tạo 2 bài đăng suất cọc mẫu");
    }
}