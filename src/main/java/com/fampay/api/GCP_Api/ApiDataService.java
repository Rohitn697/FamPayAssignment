package com.fampay.api.GCP_Api;

import com.fampay.api.repository.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ApiDataService {

  private static final Logger logger = LoggerFactory.getLogger(ApiDataService.class);

  @Autowired
  private StoreData database;

  private static final String API_URL = "https://www.googleapis.com/youtube/v3/search";

  private static final String YAML_SOURCE = "src/main/resources/application.yml";

  private final RestTemplate restTemplate = new RestTemplate();

  private List<String> keys;

  private List<String> topics;

  private AtomicInteger currentApiKeyIndex = new AtomicInteger(0);

  public void searchContent() {
    try {
      UriComponentsBuilder uri = setQueryParams();
      ResponseEntity<String> response = restTemplate.getForEntity(uri.toUriString(), String.class);
      String body = response.getBody();
      database.insert(body);
    } catch (HttpClientErrorException e) {
      handleApiRequestError(e);
    } catch (Exception e) {
      logger.error("Failed while hitting data API", e);
    }
  }

  private UriComponentsBuilder setQueryParams() {
    if (keys == null || keys.isEmpty()) {
      arrangeDetails();
    }

    int currentKeyIndex = currentApiKeyIndex.get() % keys.size();
    String key = keys.get(currentKeyIndex);
    String topic = topics.get(0);
    logger.info("Searching for Query: " + topic);
    UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(API_URL);
    uri.queryParam("key", key);
    uri.queryParam("q", topic);
    uri.queryParam("type", "video");
    uri.queryParam("part", "snippet");
    uri.queryParam("order", "date");
    uri.queryParam("maxResults", 10);

    return uri;
  }

  @SuppressWarnings("unchecked")
  private void arrangeDetails() {
    try (InputStream input = new FileInputStream(YAML_SOURCE)) {
      Yaml yaml = new Yaml();
      Map<String, Object> data = yaml.load(input);
      Map<String, Object> youtubeData = (Map<String, Object>) data.get("youtubeData");

      keys = (List<String>) youtubeData.get("key");
      topics = (List<String>) youtubeData.get("topics");

    } catch (Exception e) {
      logger.error("Connection failed ", e);
    }
  }

  private void handleApiRequestError(HttpClientErrorException e) {
    logger.error("API request failed with status code: {}", e.getStatusCode());

    // Check if the error is related to an expired API key
    if (e
        .getStatusCode()
        .is4xxClientError() && e
        .getResponseBodyAsString()
        .contains("quotaExceeded")) {
      switchToNextApiKey();
      // Retry the API request
      searchContent();
    }
  }

  private void switchToNextApiKey() {
    if (keys != null && !keys.isEmpty()) {
      int newIndex = currentApiKeyIndex.getAndIncrement();
      if (newIndex >= keys.size()) {
        logger.error("All API keys have been exhausted. Unable to make a successful API request.");
        throw new RuntimeException(
            "All API keys have been exhausted. Unable to make a successful API request.");
      }
      logger.info("Switched to the next API key");
    } else {
      logger.error("No valid API keys available");
      throw new RuntimeException("No valid API keys available");
    }
  }
}
