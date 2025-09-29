package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "오늘의 추천 조회", description = "사용자의 오늘 섭취 칼로리 기반으로 운동 및 음식 추천을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TodayRecommendationResponseDto.class)))
    })
    @GetMapping("/today")
    public ResponseEntity<TodayRecommendationResponseDto> getTodayRecommendation() {
        TodayRecommendationResponseDto dto = recommendationService.generateRecommendation();
        return ResponseEntity.ok(dto);
    }


}
