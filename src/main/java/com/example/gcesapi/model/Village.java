package com.example.gcesapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "village")
public class Village {

    @Id
    private Long villageId; // Using villageId as the primary key

    private String villageName;
    private Long villageLgdCode;
    private Long stateId;
    private String stateName;
    private Long stateLgdCode;
    private Long districtId;
    private String districtName;
    private Long districtLgdCode;
    private Long subDistrictId;
    private String subDistrictName;
    private Long subDistrictLgdCode;

    // You can add more fields from the response if needed
}