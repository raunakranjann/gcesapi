package com.example.gcesapi.controller;

import com.example.gcesapi.model.UserToken;
import com.example.gcesapi.model.Village;
import com.example.gcesapi.repository.VillageRepository;
import com.example.gcesapi.service.GcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Controller
public class DashboardController {

    private final GcesService gcesService;
    private final VillageRepository villageRepository;

    @Autowired
    public DashboardController(GcesService gcesService, VillageRepository villageRepository) {
        this.gcesService = gcesService;
        this.villageRepository = villageRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(required = false) String userName, @RequestParam(required = false) String userFullName,
                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size,
                                @RequestParam(required = false) String search, @RequestParam(required = false) String stateLgdCode,
                                Model model) {
        Optional<UserToken> userTokenOptional = gcesService.getUserTokenByUserName(userName);
        if (userTokenOptional.isEmpty()) {
            model.addAttribute("error", "You must be logged in to access the dashboard.");
            return "redirect:/login-page";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Village> villagePage;

        if (search != null && !search.isEmpty()) {
            if (stateLgdCode != null && !stateLgdCode.equals("all")) {
                villagePage = villageRepository.findByVillageNameContainingIgnoreCaseAndStateLgdCode(search, Long.parseLong(stateLgdCode), pageable);
            } else {
                villagePage = villageRepository.findByVillageNameContainingIgnoreCase(search, pageable);
            }
        } else if (stateLgdCode != null && !stateLgdCode.equals("all")) {
            villagePage = villageRepository.findByStateLgdCode(Long.parseLong(stateLgdCode), pageable);
        } else {
            villagePage = villageRepository.findAll(pageable);
        }

        model.addAttribute("villages", villagePage.getContent());
        model.addAttribute("currentPage", villagePage.getNumber());
        model.addAttribute("totalPages", villagePage.getTotalPages());
        model.addAttribute("totalItems", villagePage.getTotalElements());

        List<Village> allVillages = villageRepository.findAll();
        Set<VillageStateInfo> distinctStates;

        if (allVillages != null && !allVillages.isEmpty()) {
            distinctStates = allVillages.stream()
                    .map(village -> new VillageStateInfo(village.getStateName(), village.getStateLgdCode()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            distinctStates = Collections.emptySet();
        }

        String nameToDisplay = userTokenOptional.get().getUserFullName() != null ? userTokenOptional.get().getUserFullName() : userName;

        model.addAttribute("distinctStates", distinctStates);
        model.addAttribute("userName", userName);
        model.addAttribute("userFullName", nameToDisplay);
        model.addAttribute("search", search);
        model.addAttribute("stateLgdCode", stateLgdCode);
        return "dashboard";
    }

    public static class VillageStateInfo {
        private String name;
        private Long lgdCode;

        public VillageStateInfo(String name, Long lgdCode) {
            this.name = name;
            this.lgdCode = lgdCode;
        }

        public String getName() { return name; }
        public Long getLgdCode() { return lgdCode; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VillageStateInfo that = (VillageStateInfo) o;
            return lgdCode.equals(that.lgdCode);
        }

        @Override
        public int hashCode() {
            return lgdCode.hashCode();
        }
    }
}
