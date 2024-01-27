package com.fampay.api.GCP_Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HitApiService {

  @Autowired
  private ApiDataService dataApi;

  @Scheduled(fixedRate = 20000)
  public void performApiRequest() {
    dataApi.searchContent();
  }
}
