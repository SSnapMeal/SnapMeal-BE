package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import snapmeal.snapmeal.service.WeeklyReportService;
import snapmeal.snapmeal.web.dto.WeeklyReportResponseDto;

@RestController
@RequiredArgsConstructor
@Tag(name = "WeeklyReport", description = "주간 리포트 API")
@RequestMapping("/reports")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;


    @GetMapping("/me")
    public ResponseEntity<WeeklyReportResponseDto> getMyReport() {
        WeeklyReportResponseDto response = weeklyReportService.getMyWeeklyReport();
        return ResponseEntity.ok(response);
    }

    // ✅ 테스트용 수동 리포트 생성 API (운영에서는 삭제해도 됨)
    @PostMapping("/weekly/generate")
    public ResponseEntity<Void> generateManually()  {
        weeklyReportService.generateWeeklyReports(); // 스케줄러와 동일한 로직 호출
        return ResponseEntity.ok().build();
    }
}