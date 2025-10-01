package com.example.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.api.service.TokenService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/callback")
    public ResponseEntity<String> receiveAuthCode(@RequestParam("code") String code,
                                                  @RequestParam(value = "state", required = false) String state) {
        // 認可コードをログに出力（またはトークン取得処理へ）
        System.out.println("Received authorization code: " + code);

        // トークン取得処理へ渡す（例：サービスクラスへ）
        String accessToken = tokenService.getAccessTokenWithAuthCode(code);
        tokenService.storeToken(accessToken);

        return ResponseEntity.ok("Access Token: " + accessToken);
    }
}