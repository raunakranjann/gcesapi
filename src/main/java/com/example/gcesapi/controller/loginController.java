package com.example.gcesapi.controller;

import com.example.gcesapi.model.AuthenticationRequest;
import com.example.gcesapi.model.UserToken;
import com.example.gcesapi.service.GcesService;
import com.example.gcesapi.repository.VillageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class loginController {

    private final GcesService gcesService;
    private final VillageRepository villageRepository;

    @Autowired
    public loginController(GcesService gcesService, VillageRepository villageRepository) {
        this.gcesService = gcesService;
        this.villageRepository = villageRepository;
    }

    @GetMapping("/login-page")
    public String showLoginPage(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String loginAndRedirect(@RequestParam String userName, @RequestParam String userPassword, RedirectAttributes redirectAttributes) {
        try {
            UserToken userToken = gcesService.loginAndStoreToken(userName, userPassword);
            redirectAttributes.addAttribute("userName", userName);
            redirectAttributes.addAttribute("userFullName", userToken.getUserFullName());
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/login-page";
        }
    }
}
