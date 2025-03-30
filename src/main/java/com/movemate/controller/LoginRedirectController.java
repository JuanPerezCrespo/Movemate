package com.movemate.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginRedirectController {

    @GetMapping("/postlogin")
    public String postLogin(Authentication auth, HttpServletRequest request) {
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MONITOR"))) {
            return "redirect:/actividades/nueva";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            return "redirect:/actividades";
        } else {
            return "redirect:/";
        }
    }
}
