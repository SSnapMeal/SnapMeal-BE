package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import snapmeal.snapmeal.web.dto.PredictionResponseDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class FastApiProxyService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용

    public PredictionResponseDto sendImageUrlToFastApi(String imageUrl) throws IOException {
        // 이미지 URL을 다운로드해서 파일로 저장
        File file = downloadImageFromUrl(imageUrl);

        // 파일을 Multipart로 보내기
        String fastApiUrl = "http://api.snapmeal.store/predict";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                fastApiUrl,
                requestEntity,
                String.class
        );

        // 파일 삭제 (임시파일 정리)
        file.delete();

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("FastAPI Prediction server error: " + response.getStatusCode());
        }

        return objectMapper.readValue(response.getBody(), PredictionResponseDto.class);
    }

    private File downloadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        File tempFile = File.createTempFile("upload-", ".jpg"); // 임시 파일 생성
        try (InputStream in = url.openStream();
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            // 버퍼 이용해서 복사
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}
