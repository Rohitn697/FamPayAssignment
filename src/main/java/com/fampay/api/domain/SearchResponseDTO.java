package com.fampay.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDTO {

  private List<YoutubeDataResponse> details;

  private int currentPage;

  private long totalItems;

  private int totalPages;
}
