package com.example.ocrtest.service;

import com.example.ocrtest.controller.respon.OcrResponseDto;
import com.example.ocrtest.controller.respon.ResponseDto;
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

            // 명함 OCR 데이터 담는 변수
            String email = ""; // 이메일
            String phoneNum = ""; // 폰번호
            String tel = ""; // 회사 번호
            String fax = ""; // 팩스

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

            // TODO: 필요한 형식 더 추가하기
            // parsing
            for (int i = 0; i < txt.length; i++) {
                // 휴대폰 번호 (M)
                if (txt[i].contains("-") && txt[i].contains("M")) {
                    phoneNum = txt[i].substring(txt[i].indexOf("M"), txt[i].indexOf("M") + 14).replace("M", " ").trim();
                    System.out.println("===========phone1=========");
                    System.out.println(phoneNum);

                } else if (txt[i].contains("-") && txt[i].contains("010") || txt[i].contains("82")) {
                    System.out.println("===========phone2=========");
                    phoneNum = txt[i].trim();
                }

                // companyTel (T)
                if (txt[i].contains("-") && txt[i].contains("T")) {
                    tel = txt[i].substring(txt[i].indexOf("T"), txt[i].indexOf("T") + 14).replace("T", " ").trim();
                    System.out.println("==========companyTel1==========");
                    System.out.println(tel);
                }

                // fax (F)
                if (txt[i].contains("-") && txt[i].contains("F")) {
                    fax = txt[i].replace("F", " ").trim();
                    System.out.println("==========fax1==========");
                    System.out.println(fax);

                    if (txt[i].contains("-") || txt[i].contains("F")) {
                        fax = txt[i].replace("F", " ").trim();
                        System.out.println("==========fax2==========");
                    }
                }
                // 이메일
                if (txt[i].contains("@")) {
                    System.out.println("==========email1==========");
                    System.out.println(email);
                    email = txt[i];
                }
                // Todo: 회사 주소 유효성 검사 체크, 경우의 수 확인 ( 도로명, 번지)

            }

            // 2. 클라이언트에게 던져줄 정보
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