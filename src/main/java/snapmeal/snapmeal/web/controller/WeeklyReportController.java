package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snapmeal.snapmeal.global.ApiResponse;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.swagger.ApiErrorCodeExamples;
import snapmeal.snapmeal.service.WeeklyReportService;
import snapmeal.snapmeal.web.dto.WeeklyReportResponseDto;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "WeeklyReport", description = "주간 리포트 API")
@RequestMapping("/reports")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;


    @GetMapping("/weekly")
    @Operation(
            summary = "주간 리포트 조회 API",
            description = "사용자의 주차별 리포트를 조회합니다. " +
                    "쿼리 파라미터 weekStart(월요일 날짜)를 지정하지 않으면 기본적으로 저번 주 리포트를 반환합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.REPORT_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<WeeklyReportResponseDto>> getWeeklyReportByWeekStart(
            @Parameter(
                    description = "조회할 주차의 시작일(월요일 기준). 예시: 2025-09-15",
                    example = "2025-09-15"
            )
            @RequestParam(value = "weekStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        WeeklyReportResponseDto responseDto = weeklyReportService.getWeeklyReportByWeekStart(weekStart);
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDto));
    }



    // 테스트용 수동 리포트 생성 API
    @PostMapping("/weekly/generate")
    public ResponseEntity<Void> generateManually()  {
        weeklyReportService.generateWeeklyReports(); // 스케줄러와 동일한 로직 호출
        return ResponseEntity.ok().build();
    }
}