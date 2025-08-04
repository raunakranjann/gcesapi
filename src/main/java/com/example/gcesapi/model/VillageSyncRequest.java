package com.example.gcesapi.model;

import lombok.Data;
import java.util.List;

@Data
public class VillageSyncRequest {
    private String userName;
    private List<Integer> stateLGDCodeList;
}
