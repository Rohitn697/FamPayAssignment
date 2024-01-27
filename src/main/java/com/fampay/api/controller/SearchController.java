package com.fampay.api.controller;

import com.fampay.api.client.Client;
import com.fampay.api.domain.SearchResponseDTO;
import com.fampay.api.domain.YoutubeDataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

  @Autowired
  Client client;

  /**
   * Endpoint for searching YouTube data based on title and description with pagination.
   *
   * @param title       The title to search for.
   * @param description The description to search for.
   * @param page        The page number for pagination.
   * @param size        The number of items per page for pagination.
   * @return A ResponseEntity containing the search results and HTTP status.
   */
  @GetMapping(value = "/searchData")
  public ResponseEntity<SearchResponseDTO> searchData(@RequestParam String title,
      @RequestParam String description,
      @RequestParam(defaultValue = "${api.default-page}") int page,
      @RequestParam(defaultValue = "${api.default-size}") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<YoutubeDataResponse> responsePage = client.searchData(title, description, pageable);

    SearchResponseDTO responseDTO = new SearchResponseDTO();
    responseDTO.setDetails(responsePage.getContent());
    responseDTO.setCurrentPage(responsePage.getNumber());
    responseDTO.setTotalItems(responsePage.getTotalElements());
    responseDTO.setTotalPages(responsePage.getTotalPages());

    if (responsePage.isEmpty()) {
      return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }
}
