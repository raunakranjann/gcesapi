package com.example.gcesapi.model;
import com.example.gcesapi.model.VillageData.StateLgdCode;
import lombok.Data;

@Data
public class VillageData {
    private Long villageId;
    private String villageName;
    private Long villageLgdCode;

    private StateLgdCode stateLgdCode;
    private DistrictLgdCode districtLgdCode;
    private SubDistrictLgdCode subDistrictLgdCode;

    @Data
    public static class StateLgdCode {
        private Long stateId;
        private String stateName;
        private Long stateLgdCode;
        private Long districtCount; // ADDED
        private Long subDistrictCount; // ADDED
        private Long villageCount; // ADDED
    }

    @Data
    public static class DistrictLgdCode {
        private Long districtId;
        private String districtName;
        private Long districtLgdCode;
        private StateLgdCode stateLgdCode;
    }

    @Data
    public static class SubDistrictLgdCode {
        private Long subDistrictId;
        private String subDistrictName;
        private Long subDistrictLgdCode;
        private StateLgdCode stateLgdCode;
        private DistrictLgdCode districtLgdCode;
    }
}
