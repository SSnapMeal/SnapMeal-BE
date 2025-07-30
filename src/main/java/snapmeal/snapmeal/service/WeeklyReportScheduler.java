package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private final WeeklyReportService weeklyReportService;

    // 매주 일요일 23시 59분 실행
    @Scheduled(cron = "0 59 23 * * SUN")
    public void generateWeeklyReports() {
        weeklyReportService.generateWeeklyReports();
    }
}

