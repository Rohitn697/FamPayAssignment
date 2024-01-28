package com.fampay.api.repository;

import com.fampay.api.domain.YoutubeDataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RetrieveData {

  private static final Logger logger = LoggerFactory.getLogger(RetrieveData.class);

  private Connection connection;

  private static final String YAML_FILE_PATH = "src/main/resources/application.yml";

  private static final String SELECT_ALL_QUERY =
      "SELECT * FROM youtube_data WHERE (LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE " +
          "LOWER(?)) ORDER BY published_on DESC LIMIT ? OFFSET ?;";

  private static final String COUNT_QUERY =
      "SELECT COUNT(*) FROM youtube_data WHERE (LOWER(title) LIKE LOWER(?) OR LOWER(description) " +
          "LIKE LOWER(?));";

  /**
   * Searches for YouTube data based on the provided title and description, with pagination.
   *
   * @param title       The title to search for.
   * @param description The description to search for.
   * @param pageable    The pagination information.
   * @return A Page containing the YouTube data matching the search criteria.
   */
  public Page<YoutubeDataResponse> searchData(String title, String description, Pageable pageable) {
    try {
      if (connection == null) {
        enableConnection();
      }

      try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_QUERY)) {
        setSearchQueryParameters(statement, title, description, pageable);
        try (ResultSet resultSet = statement.executeQuery()) {
          List<YoutubeDataResponse> responseList = mapResultSetToResponseList(resultSet);
          long totalCount = getTotalCount(title, description);
          return new PageImpl<>(responseList, pageable, totalCount);
        }
      }

    } catch (SQLException exception) {
      handleSQLException(exception);
    } catch (Exception exception) {
      handleGeneralException(exception);
    }
    return Page.empty();
  }

  /**
   * Establishes a database connection based on the information provided in the YAML configuration
   * file.
   *
   * @throws SQLException If a database connection cannot be established.
   */
  private void enableConnection() throws SQLException {
    try (InputStream input = new FileInputStream(YAML_FILE_PATH)) {
      Yaml yaml = new Yaml();
      Map<String, Object> data = loadYamlData(input, yaml);

      Map<String, Object> spring = getMapFromKey(data, "spring");
      Map<String, Object> datasource = getMapFromKey(spring, "datasource");

      String url = getStringFromKey(datasource, "url");
      String username = getStringFromKey(datasource, "username");
      String password = getStringFromKey(datasource, "password");

      connection = DriverManager.getConnection(url, username, password);

    } catch (Exception e) {
      handleConnectionError(e);
    }
  }

  /**
   * Loads YAML data from an input stream.
   *
   * @param input The input stream containing YAML data.
   * @param yaml  The YAML parser.
   * @return A map representing the loaded YAML data.
   */
  private Map<String, Object> loadYamlData(InputStream input, Yaml yaml) {
    return yaml.load(input);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getMapFromKey(Map<String, Object> map, String key) {
    return (Map<String, Object>) map.get(key);
  }

  private String getStringFromKey(Map<String, Object> map, String key) {
    return (String) map.get(key);
  }

  /**
   * Maps a ResultSet to a list of YouTubeDataResponse objects.
   *
   * @param resultSet The ResultSet to map.
   * @return List of YouTubeDataResponse objects.
   * @throws SQLException If a database access error occurs.
   */
  private List<YoutubeDataResponse> mapResultSetToResponseList(ResultSet resultSet)
      throws SQLException {
    List<YoutubeDataResponse> responseList = new ArrayList<>();
    while (resultSet.next()) {
      YoutubeDataResponse content = mapResultSetToYoutubeDataResponse(resultSet);
      responseList.add(content);
    }
    return responseList;
  }

  private YoutubeDataResponse mapResultSetToYoutubeDataResponse(ResultSet resultSet)
      throws SQLException {
    YoutubeDataResponse content = new YoutubeDataResponse();
    content.setVideoId(resultSet.getString("video_id"));
    content.setDescription(resultSet.getString("description"));
    content.setTitle(resultSet.getString("title"));
    content.setTimestamp(resultSet.getTimestamp("published_on"));
    content.setThumbnails(resultSet.getString("thumbnails"));
    return content;
  }

  private long getTotalCount(String title, String description) throws SQLException {
    try (PreparedStatement countStatement = connection.prepareStatement(COUNT_QUERY)) {
      setCountQueryParameters(countStatement, title, description);
      try (ResultSet countResultSet = countStatement.executeQuery()) {
        countResultSet.next();
        return countResultSet.getLong(1);
      }
    }
  }

  private void setCountQueryParameters(PreparedStatement countStatement,
      String title,
      String description) throws SQLException {
    // Split the search query into individual words
    String[] titleWords = title
        .toLowerCase()
        .split("\\s+");
    String[] descriptionWords = description
        .toLowerCase()
        .split("\\s+");

    // Create a pattern for each word to search for partial matches
    String titlePattern = "%" + String.join("%", titleWords) + "%";
    String descriptionPattern = "%" + String.join("%", descriptionWords) + "%";

    // Set parameters for both title and description
    countStatement.setString(1, titlePattern);
    countStatement.setString(2, descriptionPattern);
  }

  private void setSearchQueryParameters(PreparedStatement statement,
      String title,
      String description,
      Pageable pageable) throws SQLException {
    // Split the search query into individual words
    String[] titleWords = title
        .toLowerCase()
        .split("\\s+");
    String[] descriptionWords = description
        .toLowerCase()
        .split("\\s+");

    // Create a pattern for each word to search for partial matches
    String titlePattern = "%" + String.join("%", titleWords) + "%";
    String descriptionPattern = "%" + String.join("%", descriptionWords) + "%";

    // Set parameters for both title and description
    statement.setString(1, titlePattern);
    statement.setString(2, descriptionPattern);
    statement.setInt(3, pageable.getPageSize());
    statement.setInt(4, pageable.getPageNumber() * pageable.getPageSize());
  }

  private void handleConnectionError(Exception e) throws SQLException {
    logger.error("Connection failed " + e.getMessage());
    throw new SQLException("Connection failed", e);
  }

  private void handleSQLException(SQLException exception) {
    logger.error("Failed while fetching the details with " + exception.getMessage());
  }

  private void handleGeneralException(Exception exception) {
    logger.error("Failed with reason " + exception.getMessage());
  }
}
