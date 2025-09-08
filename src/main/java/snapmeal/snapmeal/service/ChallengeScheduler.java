package snapmeal.snapmeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.repository.UserRepository;
import snapmeal.snapmeal.service.ChallengeGeneratorService;
import snapmeal.snapmeal.service.ChallengeService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 챌린지 스케줄러
 * - 매일 00:10 (KST): 자정 성공/실패 판정
 * - 매주 월요일 00:20 (KST): 이번 주 챌린지 3개 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ChallengeService challengeService;
    private final ChallengeGeneratorService generatorService;
    private final UserRepository userRepository;

    /** 자정 직후 평가: 어제까지 끝난 챌린지를 SUCCESS/FAIL로 마감 */
    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void evaluateDailyAtMidnight() {
        challengeService.evaluateDaily();
    }

    /** 주간 챌린지 생성: 주가 바뀐 직후(월 00:20)에 생성 */
    @Scheduled(cron = "0 20 0 * * MON", zone = "Asia/Seoul")
    public void generateWeeklyChallenges() {
        LocalDate today = LocalDate.now(KST);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<User> users = userRepository.findAll();
        for (User u : users) {
            generatorService.generateWeeklyForUser(u, weekStart, weekEnd);
        }
    }
}
