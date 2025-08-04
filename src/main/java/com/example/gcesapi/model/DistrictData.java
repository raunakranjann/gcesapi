package com.example.gcesapi.model;
import com.example.gcesapi.model.VillageData.StateLgdCode;
import lombok.Data;

@Data
public class DistrictData {
    private Long districtId;
    private String lastSyncedOn;
    private Long districtLgdCode;
    private String districtName;
    private StateLgdCode stateLgdCode; // Nested state info
    private Object districtGeometry;
}
