package com.example.gcesapi.model;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String status;
    private int code;
    private String message;
    private AuthenticationData data;

    @Data
    public static class AuthenticationData {
        private String userToken;
        private Long userId;

        private String userFullName;
    }
}
