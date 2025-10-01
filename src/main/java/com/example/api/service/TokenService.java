package com.example.api.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

@Service
public class TokenService {

    private static final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();
    private static String TOKEN = "TOKEN";

    @Value("${oauth.client-id}")
    private String clientId;

    @Value("${oauth.client-secret}")
    private String clientSecret;

    @Value("${oauth.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.token-url}")
    private String tokenUrl;

    public String getAccessTokenWithAuthCode(String authCode) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId +
                "&scope=https%3A%2F%2Fgraph.microsoft.com%2F.default" +
                "&code=" + authCode +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code" +
                "&client_secret=" + clientSecret;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);
        System.out.println("step 0 user get AccessTokenWithAuthCode. get response:"+response);
        JSONObject json = new JSONObject(response.getBody());
        return json.getString("access_token");
    }

    public void storeToken(String accessToken) {
        tokenStore.put(TOKEN, accessToken);
    }

    public String getToken() {
        return tokenStore.get(TOKEN);
    }
}