package org.auwerk.otus.arch.userservice.api.dto;

public class RegisterUserRequestDto {
    private String userName;
    private String email;

    public RegisterUserRequestDto() {
    }

    public RegisterUserRequestDto(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
