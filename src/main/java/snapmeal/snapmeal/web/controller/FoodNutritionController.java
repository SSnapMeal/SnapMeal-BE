package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snapmeal.snapmeal.service.FoodNutritionCommandService;
import snapmeal.snapmeal.service.WeeklyReportService;
import snapmeal.snapmeal.web.dto.NutritionRequestDto;
import snapmeal.snapmeal.web.dto.WeeklyReportResponseDto;

@RestController
@RequestMapping("/nutritions")
@RequiredArgsConstructor
public class FoodNutritionController {

    private final FoodNutritionCommandService foodNutritionCommandService;
    @Operation(
            summary = "음식 영양 분석 요청",
            description = """
        사용자가 선택한 음식 목록을 기반으로 OpenAI를 통해 칼로리, 단백질, 탄수화물, 당, 지방을 분석합니다.  
        분석된 데이터는 DB에 저장되며, 응답으로 총합 영양성분 정보를 반환합니다.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영양 분석 성공", content = @Content(schema = @Schema(implementation = NutritionRequestDto.TotalNutritionRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "404", description = "이미지 ID에 해당하는 이미지 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 OpenAI 응답 문제")
    })
    @PostMapping("/analyze")
    public ResponseEntity<NutritionRequestDto.TotalNutritionRequestDto> analyzeNutrition(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "음식 이름 목록과 분석할 이미지 ID",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NutritionRequestDto.FoodNutritionRequestDto.class),
            examples = @ExampleObject(
                    name = "기본 예시",
            summary = "떡볶이와 오뎅의 영양 분석 요청",
            value = "{\"foodNames\": [\"떡볶이\", \"오뎅\"], \"imageId\": 1}"
                ))
            )
            @RequestBody NutritionRequestDto.FoodNutritionRequestDto request) {
        NutritionRequestDto.TotalNutritionRequestDto result = foodNutritionCommandService.analyze(request);
        return ResponseEntity.ok(result);
    }



}
