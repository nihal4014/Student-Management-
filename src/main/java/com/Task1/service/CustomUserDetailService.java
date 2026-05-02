package com.Task1.service;

import com.Task1.model.Admin;
import com.Task1.model.Student;
import com.Task1.repository.AdminRepository;
import com.Task1.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Pehle admin check karo
        Admin admin = adminRepository.findByEmail(email);

        if (admin != null) {
            return User.withUsername(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities("ROLE_ADMIN")
                    .build();
        }

        // 2. Phir student check karo
        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student != null) {
            return User.withUsername(student.getEmail())
                    .password(student.getPassword())
                    .authorities("ROLE_STUDENT")
                    .build();
        }

        // 3. Agar dono jagah nahi mila
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}