package com.example.gcesapi.model;

import lombok.Data;
import java.util.List;

@Data
public class StateData {
    private Long stateId;
    private Long lastSyncedCount;
    private String lastSyncedOn;
    private String stateName;
    private Long stateLgdCode;
    private Object stateGeometry; // Type is often an object, not a simple string or number.
    private Long districtCount;
    private Long subDistrictCount;
    private Long villageCount;
}
