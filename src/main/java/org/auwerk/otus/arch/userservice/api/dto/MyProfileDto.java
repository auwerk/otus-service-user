package org.auwerk.otus.arch.userservice.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class MyProfileDto {
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Integer phoneNumber;
}
