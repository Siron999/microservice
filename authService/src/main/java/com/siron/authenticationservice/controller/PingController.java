package com.siron.authenticationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "Application is running - 2025/03/11";
    }
}
