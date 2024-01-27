package com.fampay.api.client;

import com.fampay.api.domain.YoutubeDataResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Client {

  Page<YoutubeDataResponse> searchData(String title, String description, Pageable pageable);
}
