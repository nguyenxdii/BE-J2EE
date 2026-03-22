package com.j2ee.carbooking.security;

import com.j2ee.carbooking.model.User;
import com.j2ee.carbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security gọi method này để load user khi xác thực
    // username ở đây là userId (chúng ta dùng userId làm subject trong JWT)
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return new org.springframework.security.core.userdetails.User(
            user.getId(),
            user.getPassword() != null ? user.getPassword() : "",
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            // ROLE_USER hoặc ROLE_ADMIN — Spring Security cần prefix ROLE_
        );
    }
}
