package com.example.gcesapi.controller;

import com.example.gcesapi.model.*;
import com.example.gcesapi.repository.VillageRepository;
import com.example.gcesapi.service.GcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.ui.Model;

@RestController
@RequestMapping("/gces")
public class GcesController {

    private final GcesService gcesService;

    public GcesController(GcesService gcesService) {
        this.gcesService = gcesService;
    }

    @PostMapping("/authenticate-and-sync-villages")
    public ResponseEntity<String> authenticateAndSyncVillages(@RequestBody AuthenticationRequestWithLgds request) {
        try {
            gcesService.loginAndStoreToken(request.getUserName(), request.getUserPassword());
            List<Village> villages = gcesService.syncVillagesByStateLgds(
                    request.getUserName(),
                    request.getStateLGDCodeList()
            );
            return ResponseEntity.ok("Authentication successful and " + villages.size() + " villages synchronized for user: " + request.getUserName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/sync-villages-by-state")
    public ResponseEntity<String> syncVillagesByState(@RequestBody VillageSyncRequest request) {
        try {
            List<Village> villages = gcesService.syncVillagesByStateLgds(
                    request.getUserName(),
                    request.getStateLGDCodeList()
            );
            return ResponseEntity.ok(villages.size() + " villages synchronized for user: " + request.getUserName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/sync-villages")
    public ResponseEntity<String> syncVillages(@RequestBody VillageSyncRequest request) {
        try {
            List<Village> villages = gcesService.syncVillagesDataWithLgds(
                    request.getUserName(),
                    request.getStateLGDCodeList(),
                    request.getDistrictLgdCodeList(),
                    request.getSubDistrictLgdCodeList()
            );
            return ResponseEntity.ok(villages.size() + " villages synchronized for user: " + request.getUserName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/sync-state")
    public ResponseEntity<String> syncState(@RequestBody StateSyncRequest request) {
        try {
            List<State> states = gcesService.syncStateData(request.getUserName());
            return ResponseEntity.ok(states.size() + " states synchronized for user: " + request.getUserName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/sync-district")
    public ResponseEntity<String> syncDistrict(@RequestBody DistrictSyncRequest request) {
        try {
            List<District> districts = gcesService.syncDistrictData(request.getUserName());
            return ResponseEntity.ok(districts.size() + " districts synchronized for user: " + request.getUserName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/sync-subDistrict")
    public ResponseEntity<String> syncSubDistrict(@RequestBody SubDistrictSyncRequest request) {
        try {
            List<SubDistrict> subDistricts = gcesService.syncSubDistrictData(request.getUserName());
            return ResponseEntity.ok(subDistricts.size() + " sub-districts synchronized for user: " + request.getUserName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/token/{userName}")
    public ResponseEntity<UserToken> getStoredToken(@PathVariable String userName) {
        try {
            return gcesService.getUserTokenByUserName(userName)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
