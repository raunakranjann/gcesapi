package com.example.gcesapi.model;

import lombok.Data;

/**
 * DTO for the request body of the state data API call.
 */
@Data
public class StateRequest {
    private String boundaryType;
    private Long userId;
}
