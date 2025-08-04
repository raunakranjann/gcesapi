package com.example.gcesapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * JPA Entity to store state data with only ID, name, and LGD code.
 */
@Entity
@Data
@Table(name = "state")
public class State {

    @Id
    private Long stateId;

    private String stateName;

    @Column(unique = true)
    private Long stateLgdCode;
}
