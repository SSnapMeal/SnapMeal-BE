package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import snapmeal.snapmeal.domain.Meals;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.global.ApiResponse;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.swagger.ApiErrorCodeExamples;
import snapmeal.snapmeal.service.MealsService;
import snapmeal.snapmeal.web.dto.MealsRequestDto;
import snapmeal.snapmeal.web.dto.MealsResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meals")
@RequiredArgsConstructor
public class MealsController {

    private final MealsService mealsService;

    @PostMapping
    @Operation(summary = "식사 기록 생성", description = "사용자가 식사(음식, 시간, 장소, 메모 등)를 기록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "식사 기록 생성 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @ApiErrorCodeExamples({
            ErrorCode.BAD_REQUEST
    })
    public ResponseEntity<ApiResponse<MealsResponseDto>> createMeal(@RequestBody MealsRequestDto request) {
        MealsResponseDto response = mealsService.createMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response));
    }

    @GetMapping("/date")
    @Operation(
            summary = "하루 식단 조회 API",
            description = "사용자의 하루 식단 목록을 조회합니다. " +
                    "쿼리 파라미터 date(yyyy-MM-dd)를 지정하지 않으면 오늘 날짜를 기준으로 조회합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.USER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<List<MealsResponseDto>>> getMealsByDate(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<MealsResponseDto> response = mealsService.getMealsByDate(date);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // 식단 기록 전체 조회
    @GetMapping
    @Operation(
            summary = "내 식사기록 전체 조회",
            description = "로그인 사용자의 모든 식사 기록을 최신순으로 반환합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 식사기록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @ApiErrorCodeExamples({
            ErrorCode.USER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<List<MealsResponseDto>>> listMyMeals(
            @AuthenticationPrincipal User loginUser
    ) {
        // 서비스에서 엔티티 목록을 가져와 DTO로 변환
        List<MealsResponseDto> result = mealsService
                .getMyMeals(loginUser)
                .stream()
                .map(MealsResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }


    @GetMapping("/{mealId}")
    @Operation(summary = "단일 식사 기록 조회", description = "특정 식사 기록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "단일 식사 기록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @ApiErrorCodeExamples({
            ErrorCode.BAD_REQUEST
    })
    public ResponseEntity<ApiResponse<MealsResponseDto>> getMeal(@PathVariable Long mealId) {
        MealsResponseDto meal = MealsResponseDto.from(mealsService.getMeal(mealId));
        return ResponseEntity.ok(ApiResponse.onSuccess(meal));
    }


    @PutMapping("/{mealId}")
    @Operation(summary = "식사 기록 수정", description = "기존 식사 기록을 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "식사 기록 수정 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
    @ApiErrorCodeExamples({
            ErrorCode.BAD_REQUEST,
            ErrorCode.UNAUTHORIZED_ACTION
    })
    public ResponseEntity<ApiResponse<MealsResponseDto>> updateMeal(@PathVariable Long mealId,
                                                                    @RequestBody MealsRequestDto requestDto) {
        Meals updatedMeal = mealsService.updateMeal(mealId, requestDto);
        MealsResponseDto response = MealsResponseDto.from(updatedMeal);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


    @DeleteMapping("/{mealId}")
    @Operation(summary = "식사 기록 삭제", description = "식사 기록을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "식사 기록 삭제 성공"
    )
    @ApiErrorCodeExamples({
            ErrorCode.BAD_REQUEST,
            ErrorCode.UNAUTHORIZED_ACTION
    })
    public ResponseEntity<Void> deleteMeal(@PathVariable Long mealId) {
        mealsService.deleteMeal(mealId);
        return ResponseEntity.noContent().build();
    }

}