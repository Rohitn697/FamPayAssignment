package com.fampay.api.repository;

import com.jayway.jsonpath.JsonPath;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StoreData {

  private static final Logger logger = LoggerFactory.getLogger(RetrieveData.class);

  private static final String CONFIG_FILE_PATH = "src/main/resources/application.yml";

  private static final String INSERT_QUERY =
      "INSERT INTO youtube_data(video_id, title, description, published_on, thumbnails) VALUES " +
          "(?, ?, ?, ?, ?);";

  private static Connection connection;

  @SuppressWarnings("unchecked")
  private void enableConnection() {
    try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
      Yaml yaml = new Yaml();
      Map<String, Object> data = yaml.load(input);

      Map<String, Object> spring = (Map<String, Object>) data.get("spring");
      Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");

      String url = (String) datasource.get("url");
      String username = (String) datasource.get("username");
      String password = (String) datasource.get("password");
      connection = DriverManager.getConnection(url, username, password);

    } catch (Exception e) {
      logger.error("Connection failed: {}", e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private void insertDataToDatabase(String videoId, JSONObject videoDetails) {
    try {
      PreparedStatement statement = connection.prepareStatement(INSERT_QUERY);

      statement.setString(1, videoId);
      statement.setString(2, videoDetails
          .get("title")
          .toString());
      statement.setString(3, videoDetails
          .get("description")
          .toString());

      String time = videoDetails
          .get("publishedAt")
          .toString();
      statement.setTimestamp(4, Timestamp.from(Instant.parse(time)));

      JSONObject thumbnails = new JSONObject();
      String defaultThumbnail = cleanUrl(videoDetails
          .get("defaultThumbnail")
          .toString());
      String mediumThumbnail = cleanUrl(videoDetails
          .get("mediumThumbnail")
          .toString());
      String highThumbnail = cleanUrl(videoDetails
          .get("highThumbnail")
          .toString());

      thumbnails.put("default", defaultThumbnail);
      thumbnails.put("medium", mediumThumbnail);
      thumbnails.put("high", highThumbnail);

      String allThumbnails = thumbnails.toJSONString();
      statement.setObject(5, allThumbnails, java.sql.Types.OTHER);

      statement.execute();
    } catch (SQLException exception) {
      handleSQLException(exception);
    } catch (Exception exception) {
      logger.error("Failed with exception: ", exception);
    }
  }

  private void handleSQLException(SQLException exception) {
    if (exception
        .getMessage()
        .contains("duplicate")) {
      logger.error("Duplicate Data is Present");
    } else {
      logger.error("Failed while inserting the data ", exception);
    }
  }

  public synchronized void insert(String body) {
    if (connection == null) {
      enableConnection();
    }

    Map<String, JSONObject> videoDetails = parseTheBody(body);
    for (String videoId : videoDetails.keySet()) {
      insertDataToDatabase(videoId, videoDetails.get(videoId));
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, JSONObject> parseTheBody(String body) {
    List<String> videoIds = JsonPath.read(body, "$.items[*].id.videoId");
    List<Object> videoData = JsonPath.read(body, "$.items[*].snippet");
    Map<String, JSONObject> dataMapper = new HashMap<>();
    for (int i = 0; i < videoIds.size(); i++) {
      String videoId = videoIds.get(i);
      Object content = videoData.get(i);
      String publishedAt = JsonPath.read(content, "$.publishedAt");
      String title = JsonPath.read(content, "$.title");
      String description = JsonPath.read(content, "$.description");
      String defaultThumbnail =
          JsonPath.read(body, "$.items[" + i + "].snippet.thumbnails.default.url");
      String mediumThumbnail =
          JsonPath.read(body, "$.items[" + i + "].snippet.thumbnails.medium.url");
      String highThumbnail = JsonPath.read(body, "$.items[" + i + "].snippet.thumbnails.high.url");

      JSONObject data = new JSONObject();
      data.put("publishedAt", publishedAt);
      data.put("title", title);
      data.put("description", description);
      data.put("defaultThumbnail", defaultThumbnail);
      data.put("mediumThumbnail", mediumThumbnail);
      data.put("highThumbnail", highThumbnail);

      dataMapper.put(videoId, data);
    }
    return dataMapper;
  }

  private String cleanUrl(String url) {
    return url.replace("\\/", "/");
  }
}
