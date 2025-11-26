// src/main/java/snapmeal/snapmeal/service/OpenAiVisionService.java
package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiVisionService {

    private static final String OPENAI_CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${spring.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * MultipartFile(업로드된 파일)로 요청
     * - Swagger에서 file 업로드로 받은 이미지 처리용
     */
    public String requestNutritionJsonFromFile(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();

            // contentType이 null일 수도 있어서 기본값 지정
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "image/jpeg";
            }

            // Base64 인코딩
            String base64 = Base64.getEncoder().encodeToString(bytes);

            String dataUrl = "data:" + contentType + ";base64," + base64;

            return callVisionWithImageUrl(dataUrl); // 공통 메서드 재사용

        } catch (Exception e) {
            log.error("파일 처리 중 오류", e);
            throw new RuntimeException("이미지 파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 공통: imageUrl 또는 dataUrl을 받아 Vision 호출
     */
    private String callVisionWithImageUrl(String imageUrlOrDataUrl) {

        // 1) HTTP 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 2) system 메시지
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                """
                너는 영양성분표를 분석하는 전문 OCR Assistant야.
                사용자가 영양성분표 이미지 한 장을 보낼 거야.
                이미지를 읽고 아래 JSON 형식으로만, 다른 말 없이 응답해.

                {
                  "calories": 숫자,
                  "protein": 숫자,
                  "carbs": 숫자,
                  "sugar": 숫자,
                  "fat": 숫자
                }

                kcal, g, mg 등의 단위는 모두 제거하고 숫자만 사용해.
                JSON 외의 설명 문장은 절대 쓰지 마.
                """
        );

        // 3) user 메시지: 텍스트 + 이미지
        Map<String, Object> userContentText = new HashMap<>();
        userContentText.put("type", "text");
        userContentText.put("text", "이 영양성분표 이미지를 분석해서 위에서 말한 JSON 형식으로만 응답해줘.");

        Map<String, Object> imageUrlObj = new HashMap<>();
        imageUrlObj.put("url", imageUrlOrDataUrl); // 여기서 URL or data URL 모두 사용 가능

        Map<String, Object> userContentImage = new HashMap<>();
        userContentImage.put("type", "image_url");
        userContentImage.put("image_url", imageUrlObj);

        List<Map<String, Object>> userContentList = new ArrayList<>();
        userContentList.add(userContentText);
        userContentList.add(userContentImage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContentList);

        // 4) 전체 요청 바디
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(systemMessage, userMessage));
        requestBody.put("max_tokens", 300);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    OPENAI_CHAT_COMPLETIONS_URL,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("OpenAI Vision 응답 코드 비정상: {}", responseEntity.getStatusCode());
                throw new RuntimeException("OpenAI Vision 호출 실패");
            }

            Map<String, Object> body = responseEntity.getBody();
            log.info("OpenAI Vision raw response: {}", body);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("OpenAI 응답에 choices가 없습니다.");
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            log.info("OpenAI Vision message.content: {}", content);
            return content;

        } catch (Exception e) {
            log.error("OpenAI Vision 호출 중 예외 발생", e);
            throw new RuntimeException("OpenAI Vision 호출 중 오류가 발생했습니다.", e);
        }
    }
}
