package com.j2ee.carbooking.dto.response;

import com.j2ee.carbooking.enums.Role;
import com.j2ee.carbooking.enums.VerifyStatus;
import com.j2ee.carbooking.model.User;
import lombok.Data;

// Trả về sau khi đăng nhập / đăng ký
// Không trả về password
@Data
public class AuthResponse {
    private String token;
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private Role role;
    private Double walletBalance;
    private VerifyStatus verifyStatus;

    public AuthResponse(String token, User user) {
        this.token = token;
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.avatar = user.getAvatar();
        this.role = user.getRole();
        this.walletBalance = user.getWalletBalance();
        this.verifyStatus = user.getIdentity() != null
            ? user.getIdentity().getVerifyStatus() : null;
    }
}
