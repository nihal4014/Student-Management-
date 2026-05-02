package com.Task1.service;

import com.Task1.model.Admin;
import com.Task1.model.Student;
import com.Task1.repository.AdminRepository;
import com.Task1.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Admin saveAdmin(Admin admin){
        if (adminRepository.findByEmail(admin.getEmail())!=null){
            throw new RuntimeException("Admin with this email already exist!");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return  adminRepository.save(admin);
    }

    public List<Student> viewAllStudents() {
        return studentRepository.findAll();
    }

    public void removeStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
