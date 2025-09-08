package snapmeal.snapmeal.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// OpenAI가 반환하는 JSON을 Jackson으로 바로 매핑하기 위한 DTO
@Getter @Setter @NoArgsConstructor
public class ChallengeAiResponse {
    private List<Item> challenges;

    @Getter @Setter @NoArgsConstructor
    public static class Item {
        private String title;       // 예: "커피 마시기"
        private String targetMenu;  // 예: "커피"
        private String description; // 예: "오늘은 아메리카노 한 잔!"
    }
}
