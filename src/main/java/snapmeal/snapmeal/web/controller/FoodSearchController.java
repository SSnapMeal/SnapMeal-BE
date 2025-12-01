package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snapmeal.snapmeal.service.FoodSearchService;
import snapmeal.snapmeal.web.dto.FoodSearchDto;

import java.util.List;

@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
@Tag(name = "Food Search API", description = "식품 영양성분 검색 API")
public class FoodSearchController {

    private final FoodSearchService foodSearchService;

    @Operation(
            summary = "식품명으로 영양성분 검색",
            description = "식품명을 입력하면 공공데이터포털 식품영양성분 DB에서 관련 영양정보를 검색합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
            }
    )
    @GetMapping("/search")
    public ResponseEntity<List<FoodSearchDto>> searchFoods(
            @Parameter(description = "검색할 식품명", example = "바나나")
            @RequestParam String query,

            @Parameter(description = "페이지 번호(기본값 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "한 페이지 결과 개수(기본값 20)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        List<FoodSearchDto> result = foodSearchService.searchFoods(query, page, size);

        // 서비스 에러 시 빈 리스트가 오는 상황에 대한 처리
        if (result.isEmpty()) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.ok(result);
    }
}
