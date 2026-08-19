package com.newspaper.library.security;

import com.newspaper.library.entity.AdminUser;
import com.newspaper.library.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for loading admin user details.
 * Used by Spring Security for authentication and authorization.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final AdminUserRepository adminUserRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AdminUser admin = adminUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    return User.builder()
            .username(admin.getUsername())
            .password(admin.getPasswordHash())
            .authorities(Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())
            ))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!admin.getEnabled())
            .build();
  }
}
