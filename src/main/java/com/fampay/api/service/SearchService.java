package com.fampay.api.service;

import com.fampay.api.client.Client;
import com.fampay.api.domain.YoutubeDataResponse;
import com.fampay.api.repository.RetrieveData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchService implements Client {

  @Autowired
  RetrieveData retrieveData;

  @Override
  public Page<YoutubeDataResponse> searchData(String title, String description, Pageable pageable) {
    return retrieveData.searchData(title, description, pageable);
  }
}
