package snapmeal.snapmeal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import snapmeal.snapmeal.web.dto.NutritionRequestDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * OpenAI가 돌려준 JSON 문자열을
     * NutritionRequestDto.TotalNutritionRequestDto로 변환
     */
    public NutritionRequestDto.TotalNutritionRequestDto parseToTotalNutrition(String jsonString) {
        try {
            log.info("Parsing nutrition JSON: {}", jsonString);

            // 정의된 DTO로 매핑
            NutritionRequestDto.TotalNutritionRequestDto dto =
                    objectMapper.readValue(jsonString, NutritionRequestDto.TotalNutritionRequestDto.class);

            log.info("Parsed DTO: {}", dto);
            return dto;
        } catch (Exception e) {
            log.error("영양성분 JSON 파싱 실패", e);
            throw new RuntimeException("영양성분 JSON 파싱 중 오류 발생", e);
        }
    }
}
