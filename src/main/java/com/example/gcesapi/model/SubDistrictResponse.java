package com.example.gcesapi.model;

import lombok.Data;
import java.util.List;

@Data
public class SubDistrictResponse {
    private String status;
    private int code;
    private String message;
    private String method;
    private List<SubDistrictData> data;
}
