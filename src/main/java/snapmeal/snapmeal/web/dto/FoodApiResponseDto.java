package snapmeal.snapmeal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 최상위 응답: { "header":{...}, "body":{...} }
@Getter
@Setter
public class FoodApiResponseDto {

    // JSON 키 이름과 동일하게 "body" 필드를 정의합니다.
    private FoodApiBody body;

    // header 필드는 필요 없으면 생략 가능합니다.
    // private ApiHeader header;

    @Getter
    @Setter
    public static class FoodApiBody {
        // 실제 API 응답의 리스트 키는 "items"이지만,
        // Service 코드에서 getRows()를 사용하므로 @JsonProperty로 매핑합니다.
        @JsonProperty("items")
        private List<FoodApiRowDto> rows;

        // 이외 필드 (선택적)
        private int page;
        private int totalCount;
        private int size;
    }

    @Getter
    @Setter
    public static class FoodApiRowDto {

        // **실제 응답 JSON 키 (AMT_NUMx)와 Service 코드에서 사용하는 필드 이름(name, kcal) 매핑**

        @JsonProperty("FOOD_NM_KR")
        private String name; // 음식 이름

        // 열량(kcal) - AMT_NUM1
        @JsonProperty("AMT_NUM1")
        private String kcal;

        // 탄수화물(g) - AMT_NUM3
        @JsonProperty("AMT_NUM3")
        private String carbo;

        // 단백질(g) - AMT_NUM4
        @JsonProperty("AMT_NUM4")
        private String protein;

        // 지방(g) - AMT_NUM5
        @JsonProperty("AMT_NUM5")
        private String fat;

        // 당류(g) - AMT_NUM6
        @JsonProperty("AMT_NUM6")
        private String sugar;

        // 나트륨(mg) - AMT_NUM12
        @JsonProperty("AMT_NUM12")
        private String sodium;
    }
}