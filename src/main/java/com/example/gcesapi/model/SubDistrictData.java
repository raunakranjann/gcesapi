package com.example.gcesapi.model;

import lombok.Data;

@Data
public class SubDistrictData {
    private Long subDistrictId;
    private String lastSyncedOn;
    private Long subDistrictLgdCode;
    private String subDistrictName;
    private StateLgdCode stateLgdCode;
    private VillageData.DistrictLgdCode districtLgdCode;
    private Object subDistrictGeometry;
}
