package com.example.api.controller;

import com.example.api.service.TeamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teamsapi")
public class TeamsController {

    @Autowired
    private TeamsService teamsService;

    @PostMapping("/sendTeamsMessage")
    public String sendTeamsMessage(@RequestParam String email, @RequestParam String message) {
        System.out.println("Received request to send message to " + email);
        System.out.println("Message: " + message);

        boolean success = teamsService.sendMessageToUser(email, message);
        return success ? "Message sent successfully" : "Failed to send message";
    }
}