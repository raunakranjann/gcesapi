package com.example.gcesapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * JPA Entity to store district data.
 */
@Entity
@Data
@Table(name = "district")
public class District {

    @Id
    private Long districtId;

    private String districtName;

    @Column(unique = true)
    private Long districtLgdCode;

    private Long stateId;

    private String stateName;

    private Long stateLgdCode;
}
