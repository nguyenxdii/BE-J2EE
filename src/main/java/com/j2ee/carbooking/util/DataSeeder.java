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
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("🌱 Đang nạp dữ liệu mẫu mới (Tiếng Việt)...");

        // Lưu ý: Không dùng deleteAll() theo yêu cầu khách hàng. 
        // Dữ liệu sẽ được nạp chồng hoặc bổ sung.

        seedUsers();
        seedCategoriesAndVehicles();
        seedOrders();
        seedListings();
        seedNotifications();

        System.out.println("✅ Hoàn tất nạp dữ liệu mẫu!");
    }

    private void seedUsers() {
        // Tạo Admin nếu chưa có
        if (userRepository.findByEmail("admin@shopcar.com").isEmpty()) {
            seedUser("admin@shopcar.com", "Quản trị viên", "123qwe123", "0901234567", 10000000.0, Role.ADMIN, true);
        }
        
        // Tạo User1 nếu chưa có
        if (userRepository.findByEmail("user1@gmail.com").isEmpty()) {
            seedUser("user1@gmail.com", "Nguyễn Văn Người Dùng 1", "123qwe123", "0911111111", 5000000.0, Role.USER, true);
        }

        // Tạo User2 nếu chưa có
        if (userRepository.findByEmail("user2@gmail.com").isEmpty()) {
            seedUser("user2@gmail.com", "Trần Thị Người Dùng 2", "123qwe123", "0922222222", 8000000.0, Role.USER, true);
        }
        
        System.out.println("  ✔ Đã kiểm tra và nạp tài khoản (Admin, User1, User2)");
    }

    private void seedUser(String email, String name, String pass, String phone, double balance, Role role, boolean verified) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(pass));
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setWalletBalance(balance);
        user.setAvatar("https://i.pravatar.cc/150?u=" + email);
        
        if (verified) {
            user.setIdentity(makeVerifiedIdentity());
        } else {
            Identity i = new Identity();
            i.setVerifyStatus(VerifyStatus.PENDING);
            user.setIdentity(i);
        }
        userRepository.save(user);
    }

    private Identity makeVerifiedIdentity() {
        Identity i = new Identity();
        i.setCccdFront("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        i.setCccdBack("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        i.setDrivingLicense("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        i.setVerifyStatus(VerifyStatus.VERIFIED);
        return i;
    }

    private void seedCategoriesAndVehicles() {
        String catGa = getOrCreateCategory("Xe tay ga", "Tiện lợi, sang trọng, cốp rộng", "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=400");
        String catSo = getOrCreateCategory("Xe số", "Tiết kiệm nhiên liệu, bền bỉ, linh hoạt", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400");
        String catCon = getOrCreateCategory("Xe côn tay", "Mạnh mẽ, tốc độ, phong cách thể thao", "https://images.unsplash.com/photo-1609630875171-b1321377ee65?w=400");

        if (vehicleRepository.count() > 0) {
            System.out.println("  ⚠ Collection vehicles đã có dữ liệu, bỏ qua bước tạo xe.");
            return;
        }

        String assetPath = "/src/assets/data/";

        vehicleRepository.saveAll(List.of(
            // 9 mẫu xe có bộ 3 ảnh
            makeVehicle("Honda Vision 2024 Trắng", catGa, "Honda", "Vision", 2024, "29A-111.11", 150000.0, 300000.0, "Dòng xe tay ga quốc dân, nhẹ nhàng, tiết kiệm xăng. Phù hợp đi làm, đi chơi trong phố.", 
                List.of(assetPath + "Honda Vision 2024 white 1.jpg", assetPath + "Honda Vision 2024 white 2.jpg", assetPath + "Honda Vision 2024 white 3.jpg"), 
                "110cc", "Xăng", "Tự động", VehicleStatus.AVAILABLE, 1200, "Hà Nội"),
            
            makeVehicle("Honda SH 150i 2024 Đen Xám", catGa, "Honda", "SH 150i", 2024, "29B-222.22", 450000.0, 1000000.0, "Mẫu xe tay ga cao cấp nhất, sang trọng và đẳng cấp. Trang bị phanh ABS an toàn.", 
                List.of(assetPath + "Honda SH150i 2024 black gray 1.png", assetPath + "Honda SH150i 2024 black gray2.jpg", assetPath + "Honda SH150i 2024 black gray3.jpg"), 
                "150cc", "Xăng", "Tự động", VehicleStatus.AVAILABLE, 500, "Hồ Chí Minh"),
            
            makeVehicle("Honda Air Blade 160 2024", catGa, "Honda", "Air Blade", 2024, "29C-333.33", 250000.0, 500000.0, "Thiết kế thể thao góc cạnh, động cơ 160cc mạnh mẽ vượt trội.", 
                List.of(assetPath + "Honda Air Blade 160 2024 1.jpg", assetPath + "Honda Air Blade 160 2024 2.jpg", assetPath + "Honda Air Blade 160 2024 3.jpg"), 
                "160cc", "Xăng", "Tự động", VehicleStatus.AVAILABLE, 2500, "Đà Nẵng"),
            
            makeVehicle("Honda Lead 2023 Bạc", catGa, "Honda", "Lead", 2023, "29D-444.44", 200000.0, 400000.0, "Cốp xe siêu rộng 37 lít, phù hợp cho phái nữ với nhu cầu để nhiều đồ đạc.", 
                List.of(assetPath + "Honda Lead 2023 silver 1.jpg", assetPath + "Honda Lead 2023 silver 2.jpg", assetPath + "Honda Lead 2023 silver 3.jpg"), 
                "125cc", "Xăng", "Tự động", VehicleStatus.AVAILABLE, 4200, "Hà Nội"),
            
            makeVehicle("Honda Wave Alpha 2024", catSo, "Honda", "Wave Alpha", 2024, "29E-555.55", 120000.0, 200000.0, "Xe số bền bỉ, tiết kiệm nhiên liệu tối đa, chi phí vận hành cực thấp.", 
                List.of(assetPath + "Honda Wave Alpha 2024 1.webp", assetPath + "Honda Wave Alpha 2024 2.webp", assetPath + "Honda Wave Alpha 2024 3.png"), 
                "110cc", "Xăng", "Số chân", VehicleStatus.AVAILABLE, 8000, "Hồ Chí Minh"),
            
            makeVehicle("Vespa Primavera S 125 Xanh Mint", catGa, "Vespa", "Primavera", 2024, "29F-666.66", 500000.0, 2000000.0, "Phong cách thời trang Ý huyền thoại, màu sắc trẻ trung độc đáo.", 
                List.of(assetPath + "Vespa Primavera S 125 mint 1.webp", assetPath + "Vespa Primavera S 125 mint 2.webp", assetPath + "Vespa Primavera S 125 mint 3.webp"), 
                "125cc", "Xăng", "Tự động", VehicleStatus.AVAILABLE, 100, "Hà Nội"),
            
            makeVehicle("Yamaha Exciter 155 VVA Xanh", catCon, "Yamaha", "Exciter", 2024, "29G-777.77", 300000.0, 600000.0, "Động cơ VVA mạnh mẽ, côn tay mượt mà cho cảm giác lái cực bốc.", 
                List.of(assetPath + "Yamaha Exciter 155 VVA blue 1.png", assetPath + "Yamaha Exciter 155 VVA blue 2.png", assetPath + "Yamaha Exciter 155 VVA blue 3.png"), 
                "155cc", "Xăng", "Côn tay", VehicleStatus.AVAILABLE, 6200, "Hồ Chí Minh"),
            
            makeVehicle("Yamaha Grande 2024 Trắng", catGa, "Yamaha", "Grande", 2024, "29H-888.88", 220000.0, 500000.0, "Mẫu xe tay ga siêu tiết kiệm xăng, động cơ Hybrid êm ái, nhẹ nhàng.", 
                List.of(assetPath + "Yamaha Grande 2024 white 1.png", assetPath + "Yamaha Grande 2024 white 2.png", assetPath + "Yamaha Grande 2024 white 3.png"), 
                "125cc", "Xăng Hybrid", "Tự động", VehicleStatus.AVAILABLE, 1100, "Đà Nẵng"),
            
            makeVehicle("Yamaha Janus 2024 Hồng", catGa, "Yamaha", "Janus", 2024, "29K-999.99", 160000.0, 300000.0, "Thiết kế trẻ trung, năng động, linh hoạt di chuyển trong ngõ nhỏ.", 
                List.of(assetPath + "Yamaha Janus 2024 pink 1.jpg", assetPath + "Yamaha Janus 2024 pink 2.jpg", assetPath + "Yamaha Janus 2024 pink 3.jpg"), 
                "125cc", "Xăng", "Tự động", VehicleStatus.AVAILABLE, 5400, "Hà Nội"),
            
            // 1 xe duy nhất có 1 ảnh
            makeVehicle("Honda Air Blade 160 Đặc biệt", catGa, "Honda", "Air Blade", 2024, "29X-000.00", 280000.0, 600000.0, "Phiên bản đặc biệt màu sơn nhám, tem xe thiết kế riêng biệt cực chất.", 
                List.of(assetPath + "Honda Air Blade 160 Đặc biệt.webp"), 
                "160cc", "Xăng", "Tự động", VehicleStatus.AVAILABLE, 50, "Cần Thơ")
        ));
        
        System.out.println("  ✔ Đã nạp 3 Danh mục và 10 Phương tiện mới.");
    }

    private String getOrCreateCategory(String name, String desc, String img) {
        return categoryRepository.findByName(name)
                .map(Category::getId)
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name);
                    c.setDescription(desc);
                    c.setImage(img);
                    return categoryRepository.save(c).getId();
                });
    }

    private Vehicle makeVehicle(String name, String categoryId, String brand, String model, int year, String licensePlate, double pricePerDay, double depositAmount, String description, List<String> images, String engine, String fuelType, String transmission, VehicleStatus status, int mileage, String location) {
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
        v.setAvgRating(0.0); // Khách yêu cầu tự đánh giá, set mặc định 0
        v.setTotalReviews(0);  // Khách yêu cầu tự đánh giá, set mặc định 0
        v.setMileage(mileage);
        v.setLocation(location);
        return v;
    }

    private void seedOrders() {
        if (orderRepository.count() > 0) return;

        User user1 = userRepository.findByEmail("user1@gmail.com").orElse(null);
        User user2 = userRepository.findByEmail("user2@gmail.com").orElse(null);
        List<Vehicle> vehicles = vehicleRepository.findAll();
        
        if (user1 == null || user2 == null || vehicles.size() < 5) return;

        // Tạo một số đơn hàng cho các xe khác nhau để có dữ liệu thống kê
        orderRepository.saveAll(List.of(
            createOrder("ORD-DEMO-01", user1.getId(), vehicles.get(0).getId(), LocalDate.now().minusDays(10), LocalDate.now().minusDays(7), OrderStatus.COMPLETED),
            createOrder("ORD-DEMO-02", user2.getId(), vehicles.get(0).getId(), LocalDate.now().minusDays(5), LocalDate.now().minusDays(2), OrderStatus.COMPLETED),
            createOrder("ORD-DEMO-03", user1.getId(), vehicles.get(1).getId(), LocalDate.now().minusDays(8), LocalDate.now().minusDays(5), OrderStatus.COMPLETED),
            createOrder("ORD-DEMO-04", user2.getId(), vehicles.get(2).getId(), LocalDate.now().minusDays(3), LocalDate.now().plusDays(2), OrderStatus.RENTING),
            createOrder("ORD-DEMO-05", user1.getId(), vehicles.get(3).getId(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), OrderStatus.CONFIRMED),
            createOrder("ORD-DEMO-06", user2.getId(), vehicles.get(4).getId(), LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), OrderStatus.PENDING)
        ));
        System.out.println("  ✔ Đã nạp 6 đơn hàng mẫu cho bản tin thống kê.");
    }

    private Order createOrder(String code, String userId, String vehicleId, LocalDate start, LocalDate end, OrderStatus status) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow();
        Order o = new Order();
        o.setOrderCode(code);
        o.setUserId(userId);
        o.setVehicleId(vehicleId);
        o.setStartDate(start);
        o.setEndDate(end);
        o.setTotalDays(3);
        o.setRentalPrice(v.getPricePerDay() * 3);
        o.setDepositAmount(v.getDepositAmount());
        o.setTotalAmount(o.getRentalPrice() + o.getDepositAmount());
        o.setStatus(status);
        o.setPaymentStatus(PaymentStatus.PAID);
        o.setCreatedAt(start.atStartOfDay().minusDays(1));
        return o;
    }

    private void seedListings() {
        if (depositListingRepository.count() > 0) return;

        User user1 = userRepository.findByEmail("user1@gmail.com").orElse(null);
        Order o1 = orderRepository.findAll().stream().findFirst().orElse(null);
        if (user1 == null || o1 == null) return;
        
        DepositListing l1 = new DepositListing();
        l1.setSellerId(user1.getId());
        l1.setOrderId(o1.getId());
        l1.setVehicleId(o1.getVehicleId());
        l1.setOriginalDeposit(o1.getDepositAmount());
        l1.setSellingPrice(o1.getDepositAmount() * 0.7);
        l1.setPlatformFee(o1.getDepositAmount() * 0.3);
        l1.setCreatedAt(LocalDateTime.now());
        l1.setExpiredAt(o1.getStartDate().atStartOfDay().minusHours(12));
        l1.setStatus(DepositListingStatus.OPEN);
        depositListingRepository.save(l1);

        System.out.println("  ✔ Đã nạp tin đăng ký gửi cọc mẫu.");
    }

    private void seedNotifications() {
        User admin = userRepository.findByEmail("admin@shopcar.com").orElse(null);
        if (admin != null) {
            Notification n1 = new Notification();
            n1.setUserId(admin.getId());
            n1.setTitle("Hệ thống đã sẵn sàng");
            n1.setMessage("Dữ liệu mẫu tiếng Việt đã được nạp thành công. Hãy bắt đầu trải nghiệm!");
            n1.setType(NotificationType.SYSTEM);
            n1.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(n1);
        }
        System.out.println("  ✔ Đã nạp thông báo hệ thống.");
    }
}