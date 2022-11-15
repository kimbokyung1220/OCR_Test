package com.example.ocrtest.controller.respon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponseDto {
  private String email;
  private String phoneNum;
  private String tel;
  private String fax;
  private String imgUrl;
}
