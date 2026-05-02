package com.Task1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "StudentData")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "fill this field")
    private String username;

    @NotBlank
    @Email(message = "Invalid Email.. please correct!")
    @Column(unique = true, nullable = false)
    private String email;

    private int age;

    private Long rollnumber;

    @ElementCollection
    private List<String> subjects;

    @CreationTimestamp
    private LocalDate admissionDate;

    private String password;

    private String role = "ROLE_STUDENT";

    @Column(name = "auth_provider")
    private String authProvider = "LOCAL";

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "admin_id")
    private Admin admin;

    private boolean isPresent = false;
}