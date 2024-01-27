package com.fampay.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeDataResponse {

  private String videoId;

  private String title;

  private String description;

  private Timestamp timestamp;

  private String thumbnails;
}
