package com.example.ffp_be.user.entity;

import jakarta.persistence.*;
import lombok.*;

import com.example.ffp_be.auth.entity.UserCredential;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_credential_id", nullable = false, unique = true)
    private UserCredential user;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer exp;
}
