package com.example.gcesapi.model;

import lombok.Data;

@Data
public class StateLgdCode {
    private Long stateId;
    private String stateName;
    private Long stateLgdCode;
    private Long districtCount;
    private Long subDistrictCount;
    private Long villageCount;
}
