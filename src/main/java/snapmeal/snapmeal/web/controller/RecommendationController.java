package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import snapmeal.snapmeal.service.TodayRecommendationService;
import snapmeal.snapmeal.web.dto.TodayRecommendationResponseDto;

import java.io.IOException;
@Tag(name = "Recommendation", description = "홈 운동/음식 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController {

    private final TodayRecommendationService recommendationService;

    @GetMapping("/today")
    public ResponseEntity<TodayRecommendationResponseDto> getTodayRecommendation() throws IOException {
        TodayRecommendationResponseDto dto = recommendationService.generateRecommendation();
        return ResponseEntity.ok(dto);
    }

}
