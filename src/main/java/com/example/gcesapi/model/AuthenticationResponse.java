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
        private String userToken; // This field was changed from 'token' to 'userToken'
        private Long userId;
        // Add other fields if present in the actual response and needed
    }
}
