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
    private String baseUrl;      // 예: https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02

    // foodApiKey를 인코딩된 키로 설정합니다.
    @Value("${food-api.key}")
    private String foodApiKey;   // 🔥 인코딩된 키 (예: QDl9tI%2B...%3D 형태)

    /**
     * 식품 영양성분 검색
     */
    public List<FoodSearchDto> searchFoods(String query, int page, int size) {
        try {
            // 1. 검색어(한글) 인코딩 처리
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            // 2. URI 경로 부분 빌드
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/getFoodNtrCpntDbInq02");

            // 3. ✨ 수정 사항: 인코딩된 serviceKey를 포함하여 쿼리 문자열을 수동으로 구성
            // UriComponentsBuilder의 queryParam을 사용하면 이미 인코딩된 serviceKey가
            // 다시 인코딩되는(이중 인코딩) 문제를 피하기 위해 직접 문자열로 조합합니다.
            // .query(fullQueryString)을 사용하면 이 문자열이 인코딩 없이 URI에 추가됩니다.
            String fullQueryString = String.format(
                    "serviceKey=%s&pageNo=%d&numOfRows=%d&type=json&FOOD_NM_KR=%s",
                    foodApiKey,   // 이미 인코딩된 키를 그대로 사용
                    page,
                    size,
                    encodedQuery  // 인코딩된 검색어 사용
            );

            // 4. 완성된 쿼리 문자열을 URI에 추가하고 URI 객체를 생성
            URI uri = builder.query(fullQueryString)
                    .build(false) // 여기서 build(false)는 인코딩 없이 URI를 완성
                    .toUri();

            log.info("식품영양성분 API 요청 URL = {}", uri);

            FoodApiResponseDto response = restTemplate.getForObject(uri, FoodApiResponseDto.class);

            if (response == null || response.getBody() == null || response.getBody().getRows() == null) {
                log.warn("API 응답이 비어있습니다.");
                return Collections.emptyList();
            }

            return response.getBody().getRows().stream()
                    .map(row -> {
                        // String 값을 Double로 변환하는 헬퍼 메서드를 사용하거나 직접 변환합니다.
                        // 값이 null이거나 숫자가 아닌 경우 오류를 방지하기 위해 안전하게 처리합니다.

                        Double kcal = safeParseDouble(row.getKcal());
                        Double carbo = safeParseDouble(row.getCarbo());
                        Double protein = safeParseDouble(row.getProtein());
                        Double fat = safeParseDouble(row.getFat());
                        Double sugar = safeParseDouble(row.getSugar());
                        Double sodium = safeParseDouble(row.getSodium());

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
            // 로그를 남겨 어떤 값이 파싱에 실패했는지 확인 가능합니다.
            log.warn("숫자 변환 실패: {}", value);
            return null;
        }
    }
}