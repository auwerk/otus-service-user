package org.auwerk.otus.arch.userservice.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfile {
    private Long id;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Integer phoneNumber;
}
