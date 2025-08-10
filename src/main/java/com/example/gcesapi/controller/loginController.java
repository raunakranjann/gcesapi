package com.example.gcesapi.controller;

import com.example.gcesapi.model.AuthenticationRequest;
import com.example.gcesapi.service.GcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class loginController {

    private final GcesService gcesService;

    @Autowired
    public loginController(GcesService gcesService) {
        this.gcesService = gcesService;
    }

    @GetMapping("/login-page")
    public String showLoginPage() {
        return "login"; // This refers to login.html
    }

    @PostMapping("/login")
    public String loginAndRedirect(@RequestParam String userName, @RequestParam String userPassword, RedirectAttributes redirectAttributes) {
        try {
            gcesService.loginAndStoreToken(userName, userPassword);
            redirectAttributes.addAttribute("userName", userName);
            redirectAttributes.addAttribute("userFullName", gcesService.getUserTokenByUserName(userName).get().getUserFullName());
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/login-page";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(required = false) String userName, @RequestParam(required = false) String userFullName, Model model) {
        if (userName == null || gcesService.getUserTokenByUserName(userName).isEmpty()) {
            model.addAttribute("error", "You must be logged in to access the dashboard.");
            return "redirect:/login-page";
        }
        model.addAttribute("userName", userName);
        model.addAttribute("userFullName", userFullName);
        return "dashboard";
    }
}
