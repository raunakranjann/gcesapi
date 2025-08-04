package com.example.gcesapi.model;

import lombok.Data;
import java.util.List;

/**
 * DTO to handle authentication request including optional LGD codes.
 */
@Data
public class AuthenticationRequestWithLgds {
    private boolean isFarmerGrievance;
    private String userName;
    private String userPassword;
    private List<Integer> stateLGDCodeList;
    private List<Integer> districtLgdCodeList;
    private List<Integer> subDistrictLgdCodeList;
}
