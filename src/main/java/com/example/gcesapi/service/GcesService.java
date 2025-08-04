package com.example.gcesapi.service;

import com.example.gcesapi.model.*;
import com.example.gcesapi.repository.UserTokenRepository;
import com.example.gcesapi.repository.VillageRepository;
import com.example.gcesapi.repository.StateRepository;
import com.example.gcesapi.repository.DistrictRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GcesService {

    private final RestTemplate restTemplate;
    private final UserTokenRepository userTokenRepository;
    private final VillageRepository villageRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;

    @Value("${gces.api.auth.url}")
    private String authApiUrl;

    @Value("${gces.api.villages.url}")
    private String villagesApiUrl;

    public GcesService(RestTemplate restTemplate, UserTokenRepository userTokenRepository, VillageRepository villageRepository, StateRepository stateRepository, DistrictRepository districtRepository) {
        this.restTemplate = restTemplate;
        this.userTokenRepository = userTokenRepository;
        this.villageRepository = villageRepository;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
    }

    public UserToken loginAndStoreToken(String userName, String userPassword) {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setFarmerGrievance(false);
        authRequest.setUserName(userName);
        authRequest.setUserPassword(userPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthenticationRequest> requestEntity = new HttpEntity<>(authRequest, headers);

        try {
            ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.exchange(
                    authApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    AuthenticationResponse.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null && "success".equals(responseEntity.getBody().getStatus())) {
                AuthenticationResponse.AuthenticationData data = responseEntity.getBody().getData();
                if (data != null && data.getUserToken() != null && data.getUserId() != null) {
                    UserToken userToken = new UserToken();
                    userToken.setUserName(userName);
                    userToken.setToken(data.getUserToken());
                    userToken.setUserId(data.getUserId());
                    userToken.setCreatedAt(LocalDateTime.now());

                    Optional<UserToken> existingToken = userTokenRepository.findByUserName(userName);
                    if (existingToken.isPresent()) {
                        UserToken tokenToUpdate = existingToken.get();
                        tokenToUpdate.setToken(data.getUserToken());
                        tokenToUpdate.setUserId(data.getUserId());
                        userTokenRepository.save(tokenToUpdate);
                        return tokenToUpdate;
                    } else {
                        return userTokenRepository.save(userToken);
                    }
                } else {
                    throw new RuntimeException("Authentication successful, but userToken or userId is missing in the response.");
                }
            } else {
                throw new RuntimeException("Authentication failed: " + responseEntity.getStatusCode() + " - " + responseEntity.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during authentication API call: " + e.getMessage(), e);
        }
    }

    // New method to sync villages using a pre-existing token and optional LGD codes.
    public List<Village> syncVillagesDataWithLgds(String userName, List<Integer> stateCodes, List<Integer> districtCodes, List<Integer> subDistrictCodes) {
        Optional<UserToken> userTokenOptional = userTokenRepository.findByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            throw new RuntimeException("No token found for user: " + userName + ". Please authenticate first.");
        }

        String token = userTokenOptional.get().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        VillageRequest villageRequest = new VillageRequest();
        villageRequest.setUserId(userTokenOptional.get().getUserId());
        villageRequest.setStateLGDCodeList(stateCodes != null && !stateCodes.isEmpty() ? stateCodes : Collections.singletonList(35));
        villageRequest.setDistrictLgdCodeList(districtCodes != null && !districtCodes.isEmpty() ? districtCodes : Collections.singletonList(603));
        villageRequest.setSubDistrictLgdCodeList(subDistrictCodes != null && !subDistrictCodes.isEmpty() ? subDistrictCodes : Collections.singletonList(5916));
        villageRequest.setBoundaryType("village");

        HttpEntity<VillageRequest> requestEntity = new HttpEntity<>(villageRequest, headers);

        try {
            ResponseEntity<VillageResponse> responseEntity = restTemplate.exchange(
                    villagesApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    VillageResponse.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null && "success".equals(responseEntity.getBody().getStatus())) {
                List<VillageData> villageDataList = responseEntity.getBody().getData();
                if (villageDataList != null && !villageDataList.isEmpty()) {
                    List<Village> villages = villageDataList.stream().map(this::mapToVillageEntity).collect(Collectors.toList());
                    return villageRepository.saveAll(villages);
                } else {
                    return Collections.emptyList();
                }
            } else {
                throw new RuntimeException("Failed to retrieve villages: " + responseEntity.getStatusCode() + " - " + responseEntity.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during village data API call: " + e.getMessage(), e);
        }
    }

    public List<State> syncStateData(String userName) {
        Optional<UserToken> userTokenOptional = userTokenRepository.findByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            throw new RuntimeException("No token found for user: " + userName + ". Please authenticate first.");
        }

        String token = userTokenOptional.get().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        StateRequest stateRequest = new StateRequest();
        stateRequest.setUserId(userTokenOptional.get().getUserId());
        stateRequest.setBoundaryType("state");

        HttpEntity<StateRequest> requestEntity = new HttpEntity<>(stateRequest, headers);

        try {
            ResponseEntity<StateResponse> responseEntity = restTemplate.exchange(
                    villagesApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    StateResponse.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null && "success".equals(responseEntity.getBody().getStatus())) {
                List<StateData> stateDataList = responseEntity.getBody().getData();
                if (stateDataList != null && !stateDataList.isEmpty()) {
                    List<State> states = stateDataList.stream().map(this::mapToStateEntity).collect(Collectors.toList());
                    return stateRepository.saveAll(states);
                } else {
                    return Collections.emptyList();
                }
            } else {
                throw new RuntimeException("Failed to retrieve state data: " + responseEntity.getStatusCode() + " - " + responseEntity.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during state data API call: " + e.getMessage(), e);
        }
    }

    public List<District> syncDistrictData(String userName) {
        Optional<UserToken> userTokenOptional = userTokenRepository.findByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            throw new RuntimeException("No token found for user: " + userName + ". Please authenticate first.");
        }

        String token = userTokenOptional.get().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setUserId(userTokenOptional.get().getUserId());
        districtRequest.setBoundaryType("district");

        HttpEntity<DistrictRequest> requestEntity = new HttpEntity<>(districtRequest, headers);

        try {
            ResponseEntity<DistrictResponse> responseEntity = restTemplate.exchange(
                    villagesApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    DistrictResponse.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null && "success".equals(responseEntity.getBody().getStatus())) {
                List<DistrictData> districtDataList = responseEntity.getBody().getData();
                if (districtDataList != null && !districtDataList.isEmpty()) {
                    List<District> districts = districtDataList.stream().map(this::mapToDistrictEntity).collect(Collectors.toList());
                    return districtRepository.saveAll(districts);
                } else {
                    return Collections.emptyList();
                }
            } else {
                throw new RuntimeException("Failed to retrieve district data: " + responseEntity.getStatusCode() + " - " + responseEntity.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during district data API call: " + e.getMessage(), e);

        }
    }

    private Village mapToVillageEntity(VillageData villageData) {
        Village village = new Village();
        village.setVillageId(villageData.getVillageId());
        village.setVillageName(villageData.getVillageName());
        village.setVillageLgdCode(villageData.getVillageLgdCode());

        if (villageData.getStateLgdCode() != null) {
            village.setStateId(villageData.getStateLgdCode().getStateId());
            village.setStateName(villageData.getStateLgdCode().getStateName());
            village.setStateLgdCode(villageData.getStateLgdCode().getStateLgdCode());
        }
        if (villageData.getDistrictLgdCode() != null) {
            village.setDistrictId(villageData.getDistrictLgdCode().getDistrictId());
            village.setDistrictName(villageData.getDistrictLgdCode().getDistrictName());
            village.setDistrictLgdCode(villageData.getDistrictLgdCode().getDistrictLgdCode());
        }
        if (villageData.getSubDistrictLgdCode() != null) {
            village.setSubDistrictId(villageData.getSubDistrictLgdCode().getSubDistrictId());
            village.setSubDistrictName(villageData.getSubDistrictLgdCode().getSubDistrictName());
            village.setSubDistrictLgdCode(villageData.getSubDistrictLgdCode().getSubDistrictLgdCode());
        }
        return village;
    }

    // UPDATED METHOD: Correctly maps StateData to State entity.
    private State mapToStateEntity(StateData stateData) {
        State state = new State();
        state.setStateId(stateData.getStateId());
        state.setStateName(stateData.getStateName());
        state.setStateLgdCode(stateData.getStateLgdCode());
        return state;
    }

    private District mapToDistrictEntity(DistrictData districtData) {
        District district = new District();
        district.setDistrictId(districtData.getDistrictId());
        district.setDistrictName(districtData.getDistrictName());
        district.setDistrictLgdCode(districtData.getDistrictLgdCode());
        if (districtData.getStateLgdCode() != null) {
            district.setStateId(districtData.getStateLgdCode().getStateId());
            district.setStateName(districtData.getStateLgdCode().getStateName());
            district.setStateLgdCode(districtData.getStateLgdCode().getStateLgdCode());
        }
        return district;
    }

    public Optional<UserToken> getUserTokenByUserName(String userName) {
        return userTokenRepository.findByUserName(userName);
    }
}
