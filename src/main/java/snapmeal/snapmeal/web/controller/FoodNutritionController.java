package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import snapmeal.snapmeal.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.swagger.ApiErrorCodeExamples;
import snapmeal.snapmeal.service.FoodNutritionService;
import snapmeal.snapmeal.web.dto.NutritionRequestDto;
import snapmeal.snapmeal.web.dto.TodayNutritionResponseDto;

@RestController
@RequestMapping("/nutritions")
@RequiredArgsConstructor
public class FoodNutritionController {

    private final FoodNutritionService foodNutritionService;

    @Operation(
            summary = "음식 영양 분석 요청",
            description = """
                사용자가 선택한 음식 목록을 기반으로 OpenAI를 통해 칼로리, 단백질, 탄수화물, 당, 지방, 나트륨을 분석합니다.  
                분석된 데이터는 DB에 저장되며, 응답으로 총합 영양성분 정보를 반환합니다.
                """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "식사 기록 생성 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)) // ✅ ApiResponse 그대로
    )
    @ApiErrorCodeExamples({
            ErrorCode.BAD_REQUEST
    })
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<NutritionRequestDto.TotalNutritionRequestDto>> analyzeNutrition(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "음식 이름 목록과 분석할 이미지 ID",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NutritionRequestDto.FoodNutritionRequestDto.class),
                            examples = @ExampleObject(
                                    name = "기본 예시",
                                    summary = "떡볶이와 오뎅의 영양 분석 요청",
                                    value = "{\"foodNames\": [\"떡볶이\", \"오뎅\"], \"imageId\": 1}"
                            )
                    )
            )
            @RequestBody NutritionRequestDto.FoodNutritionRequestDto request
    ) {
        NutritionRequestDto.TotalNutritionRequestDto result = foodNutritionService.analyze(request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @GetMapping("/today")
    @Operation(
            summary = "오늘 영양 합계 조회",
            description = "오늘 기록된 식단의 총 칼로리, 단백질, 탄수화물, 지방, 당류, 나트륨 합계를 반환합니다."
    )
    public ResponseEntity<ApiResponse<TodayNutritionResponseDto>> getTodayNutritionSummary() {
        TodayNutritionResponseDto summary = foodNutritionService.getTodaySummary();
        return ResponseEntity.ok(ApiResponse.onSuccess(summary));
    }
}