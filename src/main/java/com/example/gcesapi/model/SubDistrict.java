package com.example.gcesapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * JPA Entity to store sub-district data.
 */
@Entity
@Data
@Table(name = "sub_district")
public class SubDistrict {

    @Id
    private Long subDistrictId;

    private String subDistrictName;

    @Column(unique = true)
    private Long subDistrictLgdCode;

    private Long stateId;

    private String stateName;

    private Long stateLgdCode;

    private Long districtId;

    private String districtName;

    private Long districtLgdCode;
}
