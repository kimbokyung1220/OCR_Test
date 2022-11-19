package com.example.ocrtest.service;

import com.example.ocrtest.controller.respon.OcrResponseDto;
import com.example.ocrtest.controller.respon.ResponseDto;
import com.example.ocrtest.exception.CustomException;
import com.example.ocrtest.exception.ErrorCode;
import com.google.cloud.vision.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OcrService {

    @Value("${cloud.gcp.storage.bucket.filePath}")
    String bucketFilePath;

    private final GoogleCloudUploadService googleCloudUploadService;

    public ResponseDto<?> detectTextGcs(MultipartFile cardImg) throws IOException {
        // 첨부파일이 없을 경우
        if(cardImg.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_IMAGE_FILE);
        }

        googleCloudUploadService.upload(cardImg);

        // String filePath = "gs://your-gcs-bucket/path/toImageFile.jpg";
        String filePath = bucketFilePath + cardImg.getOriginalFilename();
        return detectTextGcs(filePath);
    }

    // Detects text in the specified remote image on Google Cloud Storage.
    public ResponseDto<?> detectTextGcs(String gcsPath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            ArrayList<Object> originList = new ArrayList<>();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    throw new IllegalArgumentException("실패");
                }

                // 사용가능한 annotations 전체 목록 참고 : http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    // 데이터를 배열에 add
                    originList.add(annotation.getDescription());
                }
            }
            // 배열의 0번째 값에 모든 데이터들이 text형식으로 담긴다
            String[] txt = originList.get(0).toString().split("\\n");

            // 명함 데이터 담는 변수
            String email = ""; // 이메일
            String phoneNum = ""; // 폰번호
            String tel = ""; // 회사 번호
            String fax = ""; // 팩스

            // TODO: 필요한 형식 더 추가하기
            // parsing 
            for (int i = 0; i < txt.length; i++) {
                // 휴대폰 번호 (M)
                if (txt[i].contains("-") && txt[i].contains("010") || txt[i].contains("82")) {

                    if (txt[i].contains("M.")) {
                        phoneNum = txt[i].replace("M.", " ").trim().substring(0, 13);
                    } else if (txt[i].contains("M")) {
                        phoneNum = txt[i].replace("M", " ").trim().substring(0, 13);
                    } else {
                        phoneNum = txt[i].trim();
                    }
                }

                // companyTel (T)
                if (txt[i].contains("-") && txt[i].contains("T.")) {
                    String telA = txt[i].substring(txt[i].indexOf("T."));
                    if (telA.length() >= 15) {
                        tel = txt[i].substring(txt[i].indexOf("T."), txt[i].indexOf("T.") + 15).replace("T.", " ").trim();
                    } else if (telA.length() < 15 || telA.length() >= 14) {
                        tel = txt[i].substring(txt[i].indexOf("T."), txt[i].indexOf("T.") + 14).replace("T.", " ").trim();
                    } else {
                        tel = txt[i].substring(txt[i].indexOf("T."), telA.length()).replace("T.", " ").trim();
                    }
                } else if (txt[i].contains("-") && txt[i].contains("T")) {
                    String telA = txt[i].substring(txt[i].indexOf("T"));
                    if (telA.length() >= 15) {
                        tel = txt[i].substring(txt[i].indexOf("T"), txt[i].indexOf("T") + 15).replace("T", " ").trim();
                    } else if (telA.length() < 15 || telA.length() >= 14) {
                        tel = txt[i].substring(txt[i].indexOf("T"), txt[i].indexOf("T") + 14).replace("T", " ").trim();
                    } else {
                        tel = txt[i].substring(txt[i].indexOf("T"), telA.length()).replace("T", " ").trim();
                    }
                }

                // fax (F)
                if (txt[i].contains("-") && txt[i].contains("F.")) {
                    String faxA = txt[i].substring(txt[i].indexOf("F."));
                    if (faxA.length() >= 15) {
                        fax = txt[i].substring(txt[i].indexOf("F."), txt[i].indexOf("F.") + 15).replace("F.", " ").trim();
                    } else if (faxA.length() < 15 || faxA.length() >= 14) {
                        fax = txt[i].substring(txt[i].indexOf("F."), txt[i].indexOf("F.") + 14).replace("F.", " ").trim();
                    } else {
                        fax = txt[i].substring(txt[i].indexOf("F."), faxA.length()).replace("F.", " ").trim();
                    }
                } else if (txt[i].contains("-") && txt[i].contains("F,")) {
                    String faxA = txt[i].substring(txt[i].indexOf("F,"));
                    if (faxA.length() >= 15) {
                        fax = txt[i].substring(txt[i].indexOf("F,"), txt[i].indexOf("F,") + 15).replace("F,", " ").trim();
                    } else if (faxA.length() < 15 || faxA.length() >= 14) {
                        fax = txt[i].substring(txt[i].indexOf("F,"), txt[i].indexOf("F,") + 14).replace("F,", " ").trim();
                    } else {
                        fax = txt[i].substring(txt[i].indexOf("F,"), faxA.length()).replace("F,", " ").trim();
                    }
                } else if (txt[i].contains("-") && txt[i].contains("F")) {
                    String faxA = txt[i].substring(txt[i].indexOf("F"));
                    if (faxA.length() >= 15) {
                        fax = txt[i].substring(txt[i].indexOf("F"), txt[i].indexOf("F") + 15).replace("F", " ").trim();
                    } else if (faxA.length() < 15 || faxA.length() >= 14) {
                        fax = txt[i].substring(txt[i].indexOf("F"), txt[i].indexOf("F") + 14).replace("F", " ").trim();
                    } else {
                        fax = txt[i].substring(txt[i].indexOf("F"), faxA.length()).replace("F", " ").trim();
                    }
                }

                // 이메일
                if (txt[i].contains("@")) {
                    if (txt[i].contains("E.")) {
                        email = txt[i].substring(txt[i].indexOf("E."), txt[i].indexOf(".com") + 4).replace("E.", " ").trim();
                    } else if (txt[i].contains("E")) {
                        email = txt[i].substring(txt[i].indexOf("E"), txt[i].indexOf(".com") + 4).replace("E", " ").trim();
                    } else {
                        email = txt[i];
                    }
                }
            }

            // 이미지를 저장하려면 repository 생성해서 save 해주기

            // 클라이언트에게 던져줄 정보
            OcrResponseDto ocrResponseDto = OcrResponseDto.builder()
                    .email(email)
                    .phoneNum(phoneNum)
                    .tel(tel)
                    .fax(fax)
                    .imgUrl(gcsPath)
                    .build();
            return ResponseDto.success(ocrResponseDto);
        }
    }
}