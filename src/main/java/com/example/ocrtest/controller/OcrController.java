package com.example.ocrtest.controller;

import com.example.ocrtest.controller.respon.ResponseDto;
import com.example.ocrtest.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api")
public class OcrController {
    private final OcrService ocrService;

    @PostMapping(value = "/scan/cards", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseDto<?> orcTest(@RequestPart(value = "cardImg", required = false) MultipartFile cardImg) throws IOException {

        return ocrService.detectTextGcs(cardImg);
    }
}
