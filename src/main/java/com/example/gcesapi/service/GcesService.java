package com.example.gcesapi.service;

import com.example.gcesapi.model.*;
import com.example.gcesapi.repository.UserTokenRepository;
import com.example.gcesapi.repository.VillageRepository;
import com.example.gcesapi.repository.StateRepository;
import com.example.gcesapi.repository.DistrictRepository;
import com.example.gcesapi.repository.SubDistrictRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;

@Service
public class GcesService {

    private final RestTemplate restTemplate;
    private final UserTokenRepository userTokenRepository;
    private final VillageRepository villageRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final SubDistrictRepository subDistrictRepository;

    @Value("${gces.api.auth.url}")
    private String authApiUrl;

    @Value("${gces.api.villages.url}")
    private String villagesApiUrl;

    public GcesService(RestTemplate restTemplate, UserTokenRepository userTokenRepository, VillageRepository villageRepository, StateRepository stateRepository, DistrictRepository districtRepository, SubDistrictRepository subDistrictRepository) {
        this.restTemplate = restTemplate;
        this.userTokenRepository = userTokenRepository;
        this.villageRepository = villageRepository;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.subDistrictRepository = subDistrictRepository;
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
                    userToken.setUserFullName(data.getUserFullName());
                    userToken.setToken(data.getUserToken());
                    userToken.setUserId(data.getUserId());
                    userToken.setCreatedAt(LocalDateTime.now());

                    Optional<UserToken> existingToken = userTokenRepository.findByUserName(userName);
                    if (existingToken.isPresent()) {
                        UserToken tokenToUpdate = existingToken.get();
                        tokenToUpdate.setToken(data.getUserToken());
                        tokenToUpdate.setUserId(data.getUserId());
                        tokenToUpdate.setUserFullName(data.getUserFullName());
                        userTokenRepository.save(tokenToUpdate);
                        return tokenToUpdate;
                    } else {
                        return userTokenRepository.save(userToken);
                    }
                } else {
                    throw new RuntimeException("Login failed: API response missing token or user ID.");
                }
            } else {
                throw new RuntimeException("Incorrect username or password.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Incorrect username or password.");
        }
    }

    public List<Village> syncVillagesDataWithLgds(String userName, List<Integer> stateCodes, List<Integer> districtCodes, List<Integer> subDistrictCodes) {
        Optional<UserToken> userTokenOptional = userTokenRepository.findByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            throw new RuntimeException("No token found for user: " + userName + ". Please authenticate first.");
        }

        if (subDistrictRepository.count() == 0) {
            throw new RuntimeException("SubDistrict data is not available. Please sync subDistrict data first.");
        }

        String token = userTokenOptional.get().getToken();
        Long userId = userTokenOptional.get().getUserId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        VillageRequest villageRequest = new VillageRequest();
        villageRequest.setUserId(userId);
        villageRequest.setStateLGDCodeList(stateCodes);
        villageRequest.setDistrictLgdCodeList(districtCodes);
        villageRequest.setSubDistrictLgdCodeList(subDistrictCodes);
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

    public List<Village> syncVillagesByStateLgds(String userName, List<Integer> stateLgdCodes) {
        Optional<UserToken> userTokenOptional = userTokenRepository.findByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            throw new RuntimeException("No token found for user: " + userName + ". Please authenticate first.");
        }

        if (subDistrictRepository.count() == 0) {
            throw new RuntimeException("SubDistrict data is not available. Please sync subDistrict data first.");
        }

        List<Long> longStateLgdCodes = stateLgdCodes.stream().mapToLong(Integer::longValue).boxed().collect(Collectors.toList());
        List<SubDistrict> subDistrictsInState = subDistrictRepository.findByStateLgdCodeIn(longStateLgdCodes);

        Set<Long> districtLgdCodes = new HashSet<>();
        Set<Long> subDistrictLgdCodes = new HashSet<>();

        for (SubDistrict sd : subDistrictsInState) {
            districtLgdCodes.add(sd.getDistrictLgdCode());
            subDistrictLgdCodes.add(sd.getSubDistrictLgdCode());
        }

        List<Integer> finalDistrictLgdCodes = districtLgdCodes.stream().map(Long::intValue).collect(Collectors.toList());
        List<Integer> finalSubDistrictLgdCodes = subDistrictLgdCodes.stream().map(Long::intValue).collect(Collectors.toList());

        return syncVillagesDataWithLgds(userName, stateLgdCodes, finalDistrictLgdCodes, finalSubDistrictLgdCodes);
    }

    public List<State> syncStateData(String userName) {
        Optional<UserToken> userTokenOptional = userTokenRepository.findByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            throw new RuntimeException("No token found for user: " + userName + ". Please authenticate first.");
        }

        String token = userTokenOptional.get().getToken();
        Long userId = userTokenOptional.get().getUserId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        StateRequest stateRequest = new StateRequest();
        stateRequest.setUserId(userId);
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
        Long userId = userTokenOptional.get().getUserId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setUserId(userId);
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

    public List<SubDistrict> syncSubDistrictData(String userName) {
        Optional<UserToken> userTokenOptional = userTokenRepository.findByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            throw new RuntimeException("No token found for user: " + userName + ". Please authenticate first.");
        }

        String token = userTokenOptional.get().getToken();
        Long userId = userTokenOptional.get().getUserId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        SubDistrictRequest subDistrictRequest = new SubDistrictRequest();
        subDistrictRequest.setUserId(userId);
        subDistrictRequest.setBoundaryType("subDistrict");

        HttpEntity<SubDistrictRequest> requestEntity = new HttpEntity<>(subDistrictRequest, headers);

        try {
            ResponseEntity<SubDistrictResponse> responseEntity = restTemplate.exchange(
                    villagesApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    SubDistrictResponse.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null && "success".equals(responseEntity.getBody().getStatus())) {
                List<SubDistrictData> subDistrictDataList = responseEntity.getBody().getData();
                if (subDistrictDataList != null && !subDistrictDataList.isEmpty()) {
                    List<SubDistrict> subDistricts = subDistrictDataList.stream().map(this::mapToSubDistrictEntity).collect(Collectors.toList());
                    return subDistrictRepository.saveAll(subDistricts);
                } else {
                    return Collections.emptyList();
                }
            } else {
                throw new RuntimeException("Failed to retrieve sub-district data: " + responseEntity.getStatusCode() + " - " + responseEntity.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during sub-district data API call: " + e.getMessage(), e);
        }
    }

    public void exportVillagesToExcel(List<Village> villages, OutputStream outputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Villages");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Village ID", "Village Name", "Village LGD Code", "State Name", "State LGD Code", "District Name", "District LGD Code", "Sub-District Name", "Sub-District LGD Code"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Fill data rows
        int rowNum = 1;
        for (Village village : villages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(village.getVillageId());
            row.createCell(1).setCellValue(village.getVillageName());
            row.createCell(2).setCellValue(village.getVillageLgdCode());
            row.createCell(3).setCellValue(village.getStateName());
            row.createCell(4).setCellValue(village.getStateLgdCode());
            row.createCell(5).setCellValue(village.getDistrictName());
            row.createCell(6).setCellValue(village.getDistrictLgdCode());
            row.createCell(7).setCellValue(village.getSubDistrictName());
            row.createCell(8).setCellValue(village.getSubDistrictLgdCode());
        }

        workbook.write(outputStream);
        workbook.close();
    }

    // NEW: Methods for fetching villages with search and filter for download
    public List<Village> findAllVillages() {
        return villageRepository.findAll();
    }

    public List<Village> findVillagesBySearch(String search) {
        return villageRepository.findByVillageNameContainingIgnoreCase(search);
    }

    public List<Village> findVillagesByStateLgdCode(Long stateLgdCode) {
        return villageRepository.findByStateLgdCode(stateLgdCode);
    }

    public List<Village> findVillagesBySearchAndStateLgdCode(String search, Long stateLgdCode) {
        return villageRepository.findByVillageNameContainingIgnoreCaseAndStateLgdCode(search, stateLgdCode);
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

    private SubDistrict mapToSubDistrictEntity(SubDistrictData subDistrictData) {
        SubDistrict subDistrict = new SubDistrict();
        subDistrict.setSubDistrictId(subDistrictData.getSubDistrictId());
        subDistrict.setSubDistrictName(subDistrictData.getSubDistrictName());
        subDistrict.setSubDistrictLgdCode(subDistrictData.getSubDistrictLgdCode());
        if (subDistrictData.getStateLgdCode() != null) {
            subDistrict.setStateId(subDistrictData.getStateLgdCode().getStateId());
            subDistrict.setStateName(subDistrictData.getStateLgdCode().getStateName());
            subDistrict.setStateLgdCode(subDistrictData.getStateLgdCode().getStateLgdCode());
        }
        if (subDistrictData.getDistrictLgdCode() != null) {
            subDistrict.setDistrictId(subDistrictData.getDistrictLgdCode().getDistrictId());
            subDistrict.setDistrictName(subDistrictData.getDistrictLgdCode().getDistrictName());
            subDistrict.setDistrictLgdCode(subDistrictData.getDistrictLgdCode().getDistrictLgdCode());
        }
        return subDistrict;
    }

    public Optional<UserToken> getUserTokenByUserName(String userName) {
        return userTokenRepository.findByUserName(userName);
    }
}