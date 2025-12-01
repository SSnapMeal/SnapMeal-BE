package snapmeal.snapmeal.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import snapmeal.snapmeal.web.dto.FoodApiResponseDto;
import snapmeal.snapmeal.web.dto.FoodSearchDto;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodSearchService {

    private final RestTemplate restTemplate;

    @Value("${food-api.base-url}")
    private String baseUrl;

    // 디코딩된 키
    @Value("${food-api.key}")
    private String foodApiKey;

    /**
     * 식품 영양성분 검색
     */
    public List<FoodSearchDto> searchFoods(String query, int page, int size) {
        try {
            // 디코딩된 키와 한글 검색어를 인코딩
            String encodedKey = URLEncoder.encode(foodApiKey, StandardCharsets.UTF_8);
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String fullQueryString = String.format(
                    "serviceKey=%s&pageNo=%d&numOfRows=%d&type=json&FOOD_NM_KR=%s",
                    encodedKey,
                    page,
                    size,
                    encodedQuery
            );

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/getFoodNtrCpntDbInq02")
                    .query(fullQueryString) // 이미 인코딩된 문자열을 그대로 사용
                    .build(true)
                    .toUri();

            log.info("raw foodApiKey = {}", foodApiKey);
            log.info("식품영양성분 API 요청 URL = {}", uri);

            FoodApiResponseDto response = restTemplate.getForObject(uri, FoodApiResponseDto.class);

            if (response == null || response.getBody() == null || response.getBody().getRows() == null) {
                log.warn("API 응답이 비어있습니다.");
                return Collections.emptyList();
            }

            return response.getBody().getRows().stream()
                    .map(row -> {
                        Double kcal    = safeParseDouble(row.getKcal());
                        Double carbo   = safeParseDouble(row.getCarbo());
                        Double protein = safeParseDouble(row.getProtein());
                        Double fat     = safeParseDouble(row.getFat());
                        Double sugar   = safeParseDouble(row.getSugar());
                        Double sodium  = safeParseDouble(row.getSodium());

                        return FoodSearchDto.builder()
                                .name(row.getName())
                                .kcal(kcal)
                                .carbo(carbo)
                                .protein(protein)
                                .fat(fat)
                                .sugar(sugar)
                                .sodium(sodium)
                                .build();
                    })
                    .toList();

        } catch (Exception e) {
            log.error("식품영양성분 API 호출 중 에러", e);
            return Collections.emptyList();
        }
    }

    /**
     * 문자열을 Double로 안전하게 변환합니다. 변환 실패 시 null을 반환합니다.
     */
    private Double safeParseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("숫자 변환 실패: {}", value);
            return null;
        }
    }
}
