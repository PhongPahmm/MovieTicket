package com.example.movieticket.model;

import com.example.movieticket.common.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User extends AbstractEntity<Integer>{
    String username;
    String email;
    String phoneNumber;
    String password;
    LocalDate dob;
    @Enumerated(EnumType.STRING)
    UserRole role;
}
