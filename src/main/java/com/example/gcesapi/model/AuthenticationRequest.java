package com.example.gcesapi.model;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private boolean isFarmerGrievance;
    private String userName;
    private String userPassword;
}