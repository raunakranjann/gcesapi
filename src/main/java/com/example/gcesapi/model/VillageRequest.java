package com.example.gcesapi.model;

import lombok.Data;
import java.util.List;

/**
 * DTO for the request body of the village data API call.
 * This class models the JSON payload required by the external API.
 */
@Data
public class VillageRequest {
    private Long userId;
    private List<Integer> stateLGDCodeList;
    private List<Integer> districtLgdCodeList;
    private List<Integer> subDistrictLgdCodeList;
    private String boundaryType;
}
