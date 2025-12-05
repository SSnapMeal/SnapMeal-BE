package snapmeal.snapmeal.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportScheduler {

    private final WeeklyReportService weeklyReportService;
    private final RedisTemplate<String, Object> redisTemplate;

    // 매주 일요일 23시 59분 실행
    @Scheduled(cron = "0 59 23 * * SUN")
    public void generateWeeklyReports() {
        weeklyReportService.generateWeeklyReports();
    }

    // 매일 00:00 (한국시간)
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void clearDailyCache() {
        log.info("[DailyCacheClear] 자정 캐시 초기화 시작");

        clearPattern("todayNutrition:*");
        clearPattern("todayRecommendation:*");

        log.info("[DailyCacheClear] 자정 캐시 초기화 완료");
    }

    private void clearPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[DailyCacheClear] 삭제된 키 개수: {} (패턴: {})", keys.size(), pattern);
        } else {
            log.info("[DailyCacheClear] 삭제할 키 없음 (패턴: {})", pattern);
        }
    }
}

