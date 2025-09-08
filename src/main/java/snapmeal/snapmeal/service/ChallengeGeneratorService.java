package snapmeal.snapmeal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.enums.ChallengeStatus;
import snapmeal.snapmeal.global.OpenAiClient;
import snapmeal.snapmeal.repository.ChallengeRepository;
import snapmeal.snapmeal.web.dto.ChallengeAiResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeGeneratorService {

    private final ChallengeRepository challengeRepository;
    private final OpenAiClient openAiClient;           // 주입 위치/타입 PR과 동일
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> FALLBACK_MENUS = List.of(
            "아메리카노","라떼","요거트","사과","바나나",
            "고구마","그릭요거트","샐러드","두부","삶은계란"
    );

    // 이번 주(weekStart~weekEnd) 챌린지가 이미 있으면 재생성하지 않음
    @Transactional
    public List<Challenges> generateWeeklyForUser(User user, LocalDate weekStart, LocalDate weekEnd) {
        boolean already = challengeRepository.existsByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                user, weekStart, weekEnd
        );
        if (already) {
            return challengeRepository.findAllByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    user, weekStart, weekEnd
            );
        }

        List<Challenges> created = fromLLMOrFallback(user, weekStart, weekEnd);
        return challengeRepository.saveAll(created);
    }

    private List<Challenges> fromLLMOrFallback(User user, LocalDate weekStart, LocalDate weekEnd) {
        try {
            String prompt = """
                너는 '일주일 건강 챌린지' 코치야.
                사용자에게 줄 '음식 기반 챌린지' 3개를 JSON으로만 응답해.
                {
                  "challenges": [
                    { "title": "커피 마시기", "targetMenu": "커피", "description": "오늘은 아메리카노 한 잔!" },
                    { "title": "그릭요거트 먹기", "targetMenu": "그릭요거트", "description": "단백질+유산균 보충" },
                    { "title": "사과 먹기", "targetMenu": "사과", "description": "식이섬유 챙기기" }
                  ]
                }
                - targetMenu는 Meals.menu에 포함 매칭 가능한 키워드여야 함.
                """;

            String raw = openAiClient.requestCompletion("당신은 건강 관리 전문가입니다.", prompt);
            // 코드블록 마커 제거 등 간단 전처리
            String cleaned = raw.replaceAll("(?s)```json|```", "").trim();

            ChallengeAiResponse parsed = objectMapper.readValue(cleaned, ChallengeAiResponse.class);

            List<Challenges> list = new ArrayList<>();
            if (parsed.getChallenges() != null) {
                parsed.getChallenges().stream().limit(3).forEach(it ->
                        list.add(Challenges.builder()
                                .user(user)
                                .title(nvl(it.getTitle(), it.getTargetMenu() + " 먹기"))
                                .targetMenuName(nvl(it.getTargetMenu(), "사과"))
                                .description(nvl(it.getDescription(), "가볍게 도전!"))
                                .startDate(weekStart)
                                .endDate(weekEnd)
                                .status(ChallengeStatus.PENDING)
                                .build())
                );
            }

            while (list.size() < 3) {
                String m = FALLBACK_MENUS.get(list.size());
                list.add(Challenges.builder()
                        .user(user)
                        .title(m + " 먹기")
                        .targetMenuName(m)
                        .description("가볍게 도전!")
                        .startDate(weekStart)
                        .endDate(weekEnd)
                        .status(ChallengeStatus.PENDING)
                        .build());
            }
            return list;

        } catch (Exception e) {
            log.warn("LLM 생성 실패. fallback 사용: {}", e.toString());
            List<Challenges> list = new ArrayList<>();
            Collections.shuffle(FALLBACK_MENUS);
            for (int i = 0; i < 3; i++) {
                String m = FALLBACK_MENUS.get(i);
                list.add(Challenges.builder()
                        .user(user)
                        .title(m + " 먹기")
                        .targetMenuName(m)
                        .description("가볍게 도전!")
                        .startDate(weekStart)
                        .endDate(weekEnd)
                        .status(ChallengeStatus.PENDING)
                        .build());
            }
            return list;
        }
    }

    private static String nvl(String s, String def) { return (s == null || s.isBlank()) ? def : s; }
}
