package com.manolito.dashflow.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manolito.dashflow.dto.dw.AuthDTO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SparkUtils {

    private static final String API_URL = "https://api.taiga.io/api/v1/auth";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String fetchDataFromEndpoint(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create an HTTP GET request
            HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "application/json");

            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }

            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching data from endpoint: " + url, e);
        }
    }

    public String authenticateTaiga(String username, String password) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(API_URL);

            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json");

            AuthDTO authRequest = AuthDTO.builder()
                    .username(username)
                    .password(password)
                    .type("normal")
                    .build();

            String jsonBody = objectMapper.writeValueAsString(authRequest);

            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Falha na autenticação: " + response.getStatusLine().getStatusCode());
            }

            String responseString = EntityUtils.toString(response.getEntity());
            JsonNode jsonNode = objectMapper.readTree(responseString);

            return jsonNode.get("auth_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao autenticar no Taiga", e);
        }
    }
}