package com.manolito.dashflow.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SparkUtils {

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

}