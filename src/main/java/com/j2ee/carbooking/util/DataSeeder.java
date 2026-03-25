package com.j2ee.carbooking.util;

import com.j2ee.carbooking.enums.*;
import com.j2ee.carbooking.model.*;
import com.j2ee.carbooking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final DepositListingRepository depositListingRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      VehicleRepository vehicleRepository,
                      OrderRepository orderRepository,
                      DepositListingRepository depositListingRepository,
                      WalletTransactionRepository walletTransactionRepository,
                      NotificationRepository notificationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.depositListingRepository = depositListingRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("🌱 Kiểm tra và nạp dữ liệu mẫu...");

        depositListingRepository.deleteAll();
        walletTransactionRepository.deleteAll();
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        seedUsers();
        seedCategories();
        seedVehicles();
        seedOrders();
        seedListings();
        seedNotifications();

        System.out.println("✅ Hoàn tất quá trình làm sạch và nạp lại dữ liệu mẫu!");
    }

    private void seedUsers() {
        User admin = new User();
        admin.setFullName("Admin ShopCar");
        admin.setEmail("admin@shopcar.com");
        admin.setPassword(passwordEncoder.encode("123qwe123"));
        admin.setPhone("0901234567");
        admin.setAvatar("https://i.pravatar.cc/150?img=1");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setWalletBalance(10000000.0);
        userRepository.save(admin);

        User user1 = new User();
        user1.setFullName("Nguyễn Văn An");
        user1.setEmail("an.nguyen@gmail.com");
        user1.setPassword(passwordEncoder.encode("123456"));
        user1.setPhone("0912345678");
        user1.setAvatar("https://i.pravatar.cc/150?img=2");
        user1.setRole(Role.USER);
        user1.setStatus(UserStatus.ACTIVE);
        user1.setWalletBalance(5000000.0);
        user1.setIdentity(makeVerifiedIdentity());
        userRepository.save(user1);

        User user2 = new User();
        user2.setFullName("Trần Thị Bích");
        user2.setEmail("bich.tran@gmail.com");
        user2.setPassword(passwordEncoder.encode("123456"));
        user2.setPhone("0923456789");
        user2.setAvatar("https://i.pravatar.cc/150?img=5");
        user2.setRole(Role.USER);
        user2.setStatus(UserStatus.ACTIVE);
        user2.setWalletBalance(8000000.0);
        user2.setIdentity(makeVerifiedIdentity());
        userRepository.save(user2);

        User user3 = new User();
        user3.setFullName("Lê Minh Cường");
        user3.setEmail("cuong.le@gmail.com");
        user3.setPassword(passwordEncoder.encode("123456"));
        user3.setPhone("0934567890");
        user3.setAvatar("https://i.pravatar.cc/150?img=8");
        user3.setRole(Role.USER);
        user3.setStatus(UserStatus.ACTIVE);
        user3.setWalletBalance(2000000.0);
        Identity identity3 = new Identity();
        identity3.setCccdFront("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        identity3.setCccdBack("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        identity3.setDrivingLicense("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        identity3.setVerifyStatus(VerifyStatus.PENDING);
        user3.setIdentity(identity3);
        userRepository.save(user3);

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
        i.setCccdFront("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        i.setCccdBack("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        i.setDrivingLicense("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        i.setVerifyStatus(VerifyStatus.VERIFIED);
        return i;
    }

    private void seedCategories() {
        categoryRepository.saveAll(List.of(
            makeCategory("Xe số", "Xe máy số tiết kiệm nhiên liệu", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"),
            makeCategory("Xe ga", "Xe tay ga tiện lợi", "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=400"),
            makeCategory("Xe côn tay", "Xe côn tay mạnh mẽ", "https://images.unsplash.com/photo-1609630875171-b1321377ee65?w=400")
        ));
        System.out.println("  ✔ Seeded 3 categories");
    }

    private Category makeCategory(String name, String description, String image) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        c.setImage(image);
        return c;
    }

    private void seedVehicles() {
        String catSo = categoryRepository.findByName("Xe số").orElseThrow().getId();
        String catGa = categoryRepository.findByName("Xe ga").orElseThrow().getId();
        String catCon = categoryRepository.findByName("Xe côn tay").orElseThrow().getId();

        vehicleRepository.saveAll(List.of(
            makeVehicle("Honda Wave Alpha", catSo, "Honda", "Wave", 2022, "51A-11111", 150000.0, 500000.0, "Xe Wave giá rẻ", List.of("https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"), "110cc", "Xăng", "Số", VehicleStatus.AVAILABLE, 4.5, 10),
            makeVehicle("Honda Vision", catGa, "Honda", "Vision", 2023, "51B-22222", 200000.0, 1000000.0, "Xe ga phổ thông", List.of("https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=400"), "110cc", "Xăng", "Ga", VehicleStatus.AVAILABLE, 4.7, 15),
            makeVehicle("Yamaha Exciter", catCon, "Yamaha", "Exciter", 2022, "51C-33333", 250000.0, 2000000.0, "Xe côn tay thể thao", List.of("https://images.unsplash.com/photo-1609630875171-b1321377ee65?w=400"), "155cc", "Xăng", "Côn", VehicleStatus.AVAILABLE, 4.9, 20),
            makeVehicle("Honda SH 150i", catGa, "Honda", "SH", 2023, "51D-44444", 500000.0, 5000000.0, "Xe ga cao cấp", List.of("https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=400"), "150cc", "Xăng", "Ga", VehicleStatus.AVAILABLE, 5.0, 5)
        ));
        System.out.println("  ✔ Seeded 4 vehicles");
    }

    private Vehicle makeVehicle(String name, String categoryId, String brand, String model, int year, String licensePlate, double pricePerDay, double depositAmount, String description, List<String> images, String engine, String fuelType, String transmission, VehicleStatus status, double avgRating, int totalReviews) {
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

    private void seedOrders() {
        User an = userRepository.findByEmail("an.nguyen@gmail.com").orElseThrow();
        User bich = userRepository.findByEmail("bich.tran@gmail.com").orElseThrow();
        List<Vehicle> vehicles = vehicleRepository.findAll();

        // Orders for Dashboard & Statistics
        orderRepository.save(createOrder("ORD001", an.getId(), vehicles.get(0).getId(), LocalDate.now().minusDays(5), LocalDate.now().minusDays(2), OrderStatus.COMPLETED));
        orderRepository.save(createOrder("ORD002", bich.getId(), vehicles.get(1).getId(), LocalDate.now().minusDays(4), LocalDate.now().minusDays(1), OrderStatus.COMPLETED));
        orderRepository.save(createOrder("ORD003", an.getId(), vehicles.get(2).getId(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), OrderStatus.RENTING));
        orderRepository.save(createOrder("ORD004", bich.getId(), vehicles.get(3).getId(), LocalDate.now(), LocalDate.now().plusDays(3), OrderStatus.PENDING));
        orderRepository.save(createOrder("ORD005", an.getId(), vehicles.get(0).getId(), LocalDate.now().minusDays(10), LocalDate.now().minusDays(8), OrderStatus.CANCELLED));
        
        System.out.println("  ✔ Seeded 5 orders with various statuses");
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
        o.setCreatedAt(start.atStartOfDay().minusHours(2));
        return o;
    }

    private void seedListings() {
        User an = userRepository.findByEmail("an.nguyen@gmail.com").orElseThrow();
        User bich = userRepository.findByEmail("bich.tran@gmail.com").orElseThrow();
        Vehicle v1 = vehicleRepository.findAll().get(0);
        Vehicle v2 = vehicleRepository.findAll().get(1);

        DepositListing l1 = new DepositListing();
        l1.setSellerId(an.getId());
        l1.setOrderId("MOCK_ORDER_ID_1");
        l1.setVehicleId(v1.getId());
        l1.setOriginalDeposit(v1.getDepositAmount());
        l1.setSellingPrice(v1.getDepositAmount() * 0.8);
        l1.setPlatformFee(v1.getDepositAmount() * 0.05);
        l1.setCreatedAt(LocalDateTime.now().minusDays(2));
        l1.setStatus(DepositListingStatus.OPEN);
        depositListingRepository.save(l1);

        DepositListing l2 = new DepositListing();
        l2.setSellerId(bich.getId());
        l2.setOrderId("MOCK_ORDER_ID_2");
        l2.setVehicleId(v2.getId());
        l2.setOriginalDeposit(v2.getDepositAmount());
        l2.setSellingPrice(v2.getDepositAmount() * 0.9);
        l2.setPlatformFee(v2.getDepositAmount() * 0.05);
        l2.setStatus(DepositListingStatus.SOLD);
        l2.setBuyerId(an.getId());
        l2.setSoldAt(LocalDateTime.now().minusDays(1));
        l2.setCreatedAt(LocalDateTime.now().minusDays(5));
        depositListingRepository.save(l2);

        System.out.println("  ✔ Seeded 2 deposit listings (1 SOLD)");
    }

    private void seedNotifications() {
        User admin = userRepository.findByEmail("admin@shopcar.com").orElse(null);
        if (admin != null) {
            Notification n1 = new Notification();
            n1.setUserId(admin.getId());
            n1.setTitle("Yêu cầu duyệt CCCD mới");
            n1.setMessage("Người dùng Lê Minh Cường vừa gửi yêu cầu xác minh danh tính. Vui lòng xét duyệt.");
            n1.setType(NotificationType.VERIFY);
            n1.setCreatedAt(LocalDateTime.now().minusHours(2));
            notificationRepository.save(n1);

            Notification n2 = new Notification();
            n2.setUserId(admin.getId());
            n2.setTitle("Đơn đặt xe mới");
            n2.setMessage("Một đơn hàng thuê xe mới vừa được tạo và đang chờ xác nhận.");
            n2.setType(NotificationType.ORDER);
            n2.setCreatedAt(LocalDateTime.now().minusMinutes(15));
            notificationRepository.save(n2);
        }
        System.out.println("  ✔ Seeded admin notifications");
    }
}