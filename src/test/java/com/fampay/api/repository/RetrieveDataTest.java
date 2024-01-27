package com.fampay.api.repository;

import com.fampay.api.domain.YoutubeDataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class RetrieveDataTest {

  @Mock
  private Connection connection;

  @InjectMocks
  RetrieveData retrieveData;

  @Test
  void searchData_SuccessfulQuery_ReturnsPageWithResults() throws Exception {
    String title = "testTitle";
    String description = "testDescription";
    Pageable pageable = mock(Pageable.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next())
        .thenReturn(true)
        .thenReturn(false);
    when(resultSet.getLong(1)).thenReturn(1L);
    when(resultSet.getString("video_id")).thenReturn("testVideoId");

    Page<YoutubeDataResponse> resultPage = retrieveData.searchData(title, description, pageable);

    assertEquals(1L, resultPage.getTotalElements());
  }

}
