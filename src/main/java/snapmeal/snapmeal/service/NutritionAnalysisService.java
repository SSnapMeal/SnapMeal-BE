package snapmeal.snapmeal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.web.dto.NutritionOcrResponseDto;
import snapmeal.snapmeal.global.util.AuthService; // <- 프로젝트에 이미 있을 가능성 높음

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionAnalysisService {

    private final OpenAiVisionService openAiVisionService;          // 🔍 OpenAI Vision 호출 담당
    private final NutritionAnalysisRepository nutritionRepository;  // 💾 JPA 레포지토리
    private final AuthService authService;                          // 👤 현재 로그인 유저 가져오기

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 영양성분표 이미지를 OCR → NutritionAnalysis 저장 → nutritionId 포함 DTO 반환
     */
    @Transactional
    public NutritionOcrResponseDto analyzeAndSave(MultipartFile file, Images image) {
        try {
            // 1) 현재 로그인한 유저
            User user = authService.getCurrentUser();

            // 2) OpenAI Vision 호출 → JSON 문자열 (예: {"calories": 130, "protein": 4, ...})
            String json = openAiVisionService.requestNutritionJsonFromFile(file);
            log.info("OpenAI Nutrition JSON = {}", json);

            // 3) JSON 문자열 → Map으로 파싱
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            // 4) NutritionAnalysis 엔티티 생성
            NutritionAnalysis nutrition = NutritionAnalysis.builder()
                    // ⚠️ foodNames는 NOT NULL이라서 임시값이라도 반드시 채워야 함
                    .foodNames("영양성분표 OCR 분석") // TODO: 음식 이름 분석 시 실제 이름으로 교체해도 됨
                    .calories(toInteger(map.get("calories")))
                    .protein(toDouble(map.get("protein")))
                    .carbs(toDouble(map.get("carbs")))
                    .sugar(toDouble(map.get("sugar")))
                    .fat(toDouble(map.get("fat")))
                    .sodium(toDouble(map.get("sodium")))
                    .image(image)   // 이미지 엔티티(이미 S3 업로드 후 생성된 것) 넘겨받으면 세팅
                    .user(user)
                    .build();

            // 5) DB 저장 → id(auto increment) 생성됨
            NutritionAnalysis saved = nutritionRepository.save(nutrition);

            // 6) DTO로 변환해서 반환 (nutritionId 포함)
            return NutritionOcrResponseDto.builder()
                    .nutritionId(saved.getId().intValue())
                    .calories(saved.getCalories())
                    .protein(saved.getProtein())
                    .carbs(saved.getCarbs())
                    .sugar(saved.getSugar())
                    .fat(saved.getFat())
                    .sodium(saved.getSodium())
                    .build();

        } catch (Exception e) {
            log.error("영양성분표 OCR 분석/저장 중 오류", e);
            throw new RuntimeException("영양성분표 분석 중 오류가 발생했습니다.", e);
        }
    }

    // ---- 🔽 안전한 파싱용 유틸 메서드들 ----

    private Integer toInteger(Object value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            log.warn("Integer 변환 실패: {}", value);
            return null;
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            log.warn("Double 변환 실패: {}", value);
            return null;
        }
    }
}
