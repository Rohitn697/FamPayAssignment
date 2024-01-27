package com.fampay.api.controller;

import com.fampay.api.client.Client;
import com.fampay.api.domain.SearchResponseDTO;
import com.fampay.api.domain.YoutubeDataResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SearchControllerTest {

  @Mock
  private Client client;

  @InjectMocks
  private SearchController searchController;

  @Test
  void searchData_ReturnsResponseEntityWithHttpStatusOK() {
    String title = "testTitle";
    String description = "testDescription";
    int page = 0;
    int size = 10;
    Page<YoutubeDataResponse> responsePage = Mockito.mock(Page.class);
    Mockito
        .when(client.searchData(title, description, PageRequest.of(page, size)))
        .thenReturn(responsePage);

    ResponseEntity<SearchResponseDTO> responseEntity =
        searchController.searchData(title, description, page, size);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  void searchData_ReturnsResponseEntityWithHttpStatusNotFound() {
    String title = "nonExistentTitle";
    String description = "nonExistentDescription";
    int page = 0;
    int size = 10;
    Page<YoutubeDataResponse> emptyPage = Mockito.mock(Page.class);
    Mockito
        .when(emptyPage.isEmpty())
        .thenReturn(true);
    Mockito
        .when(client.searchData(title, description, PageRequest.of(page, size)))
        .thenReturn(emptyPage);

    ResponseEntity<SearchResponseDTO> responseEntity =
        searchController.searchData(title, description, page, size);
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
  }
}
