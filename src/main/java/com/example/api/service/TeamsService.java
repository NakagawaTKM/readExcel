package com.example.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;

@Service
public class TeamsService {

    // get value from application.properties
    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.send-user-id}")
    private String sendUserId;

    private String getTokenUrl() {
        return "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
    }

    private final String graphApiUrl = "https://graph.microsoft.com/v1.0";

    public boolean sendMessageToUser(String email, String message) {
        try {
            // 1. get access token
            String accessToken = getAccessToken();
            System.out.println("step1 getAccessToken. Result AccessToken:" + accessToken);

            // 2. find user id by email
            String userId = getUserIdByEmail(email, accessToken);
            System.out.println("email:" + email);
            System.out.println("step 2 getUserIdByEmail. Result UserID:" + userId);

            if (userId == null) {
                System.out.println("step 2 getUserIdByEmail faild. Please check the Email again. Email:" + email);
                return false;
            }
            // 3. send Teams message
            return sendTeamsMessage(userId, message, accessToken);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId +
                "&scope=https%3A%2F%2Fgraph.microsoft.com%2F.default" +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(getTokenUrl(), HttpMethod.POST, entity, String.class);
        JSONObject json = new JSONObject(response.getBody());
        return json.getString("access_token");
    }

    private String getUserIdByEmail(String email, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = graphApiUrl + "/users/" + email;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject json = new JSONObject(response.getBody());
            return json.getString("id");
        }
        return null;
    }

    private boolean sendTeamsMessage(String userId, String message, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // get chatId
        String chatId = null;
        String chatUrl = graphApiUrl + "/users/" + userId + "/chats";
        HttpEntity<Void> chatEntity = new HttpEntity<>(headers);
        ResponseEntity<String> chatResponse = restTemplate.exchange(chatUrl,
                HttpMethod.GET, chatEntity, String.class);
        System.out.println("step 3.0 Get chatId result:"+chatResponse);
        if (chatResponse.getStatusCode() == HttpStatus.OK) {
            JSONObject chatJson = new JSONObject(chatResponse.getBody());
            if (chatJson.has("value") && chatJson.getJSONArray("value").length() > 0) {
                chatId = chatJson.getJSONArray("value").getJSONObject(0).getString("id");
            }
        }
        System.out.println("step 3.1 get chatId successed. ChatId is:" + chatId);
        chatId=null;
        // if not have exist chatId，create new 1:1 chat
        if (chatId == null) {
            String createChatUrl = graphApiUrl + "/chats";
            JSONObject chatBody = new JSONObject();
            chatBody.put("chatType", "oneOnOne");

            org.json.JSONArray members = new org.json.JSONArray();
            members.put(new JSONObject()
                    .put("@odata.type", "#microsoft.graph.aadUserConversationMember")
                    .put("roles", new org.json.JSONArray().put("owner"))
                    .put("user@odata.bind", graphApiUrl + "/users/" + sendUserId) // Nakagawa ID fixed
            );

            members.put(new JSONObject()
                    .put("@odata.type", "#microsoft.graph.aadUserConversationMember")
                    .put("roles", new org.json.JSONArray().put("owner"))
                    .put("user@odata.bind", graphApiUrl + "/users/" + userId)
            );

            chatBody.put("members", members);
            System.out.println("members"+members.toString());

            HttpEntity<String> createChatEntity = new HttpEntity<>(chatBody.toString(), headers);
            ResponseEntity<String> createChatResponse = restTemplate.exchange(createChatUrl, HttpMethod.POST,
                    createChatEntity, String.class);
            if (createChatResponse.getStatusCode() == HttpStatus.CREATED
                    || createChatResponse.getStatusCode() == HttpStatus.OK) {
                JSONObject createdChatJson = new JSONObject(createChatResponse.getBody());
                chatId = createdChatJson.getString("id");
                System.out.println("step 3.2 Create chatId successed. ChatId is:" + chatId);
            } else {
                return false;
            }
        }

        // send message to chat
        String msgUrl = graphApiUrl + "/chats/" + chatId + "/messages";
        JSONObject msgBody = new JSONObject();
        msgBody.put("body", new JSONObject().put("content", message));

        HttpEntity<String> msgEntity = new HttpEntity<>(msgBody.toString(), headers);
        ResponseEntity<String> msgResponse = restTemplate.exchange(msgUrl, HttpMethod.POST, msgEntity, String.class);

        return msgResponse.getStatusCode() == HttpStatus.CREATED;
    }
}