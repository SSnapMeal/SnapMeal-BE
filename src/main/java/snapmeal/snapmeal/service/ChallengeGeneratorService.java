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
            "간장게장","갈비찜","깍두기","떡꼬치","도토리묵 무침","동그랑땡",
            "된장찌개","두부김치","두부조림","떡만두국","떡볶이","만두","배추김치","시래기 된장국","식혜",
            "애호박볶음","약과","약밥","양념게장","어묵볶음","열무국수","열무김치","오징어채볶음","오징어튀김","육개장",
            "잔치국수","잡채","전복죽","전통 한과","제육볶음","족발","진미채볶음","짜장면","짬뽕","쫄면",
            "추어탕","치킨","칼국수","콩국수","콩나물국","콩나물무침","콩자반","숙주나물무침","파전","편육","해물찜","호박전"
    );

    // 이번 주(weekStart~weekEnd) 챌린지가 이미 있으면 재생성하지 않음
    @Transactional
    public List<Challenges> generateWeeklyForUser(User user, LocalDate weekStart, LocalDate weekEnd, boolean force) {
        if (force) {
            // ✅ 이번 주 이미 생성된 것들 싹 지우고 다시 만든다
            challengeRepository.deleteAllByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    user, weekStart, weekEnd
            );
        } else {
            boolean already = challengeRepository
                    .existsByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(user, weekStart, weekEnd);
            if (already) {
                return challengeRepository
                        .findAllByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(user, weekStart, weekEnd);
            }
        }

        List<Challenges> created = fromLLMOrFallback(user, weekStart, weekEnd);
        return challengeRepository.saveAll(created);
    }

    private List<Challenges> fromLLMOrFallback(User user, LocalDate weekStart, LocalDate weekEnd) {
        // 오직 FALLBACK_MENUS 안에서만 뽑기
        List<String> allowed = FALLBACK_MENUS;
        List<Challenges> list = new ArrayList<>();

        try {
            String allowedJson = "[\"" + String.join("\",\"", allowed) + "\"]";

            String system = "너는 사용자에게 주간 '음식 기반 챌린지'를 제안하는 코치야. "
                    + "반드시 내가 준 허용 메뉴 목록 안에서만 선택하고, JSON 외 텍스트는 절대 반환하지 마.";

            String prompt = """
            아래 allowedMenus 안에서만 정확히 3개의 챌린지를 JSON으로 반환해.
            스키마:
            {
              "challenges": [
                { "title": "string", "targetMenu": "string", "description": "string" }
              ]
            }
            제약:
            - allowedMenus 밖 메뉴 사용 금지
            - 주류 추천 금지
            - 응답은 JSON 한 덩어리만 (코드블록/주석/설명 금지)
            - title은 {menu} 먹기 형식을 권장 (회피형이 아니라면)
            allowedMenus: %s
            """.formatted(allowedJson);

            String raw = openAiClient.requestCompletion(system, prompt);
            String cleaned = raw.replaceAll("(?s)```json|```", "").trim();

            ChallengeAiResponse parsed = objectMapper.readValue(cleaned, ChallengeAiResponse.class);

            // 2차 필터: 허용 메뉴 밖/중복 제거
            if (parsed.getChallenges() != null) {
                var used = new java.util.HashSet<String>();
                parsed.getChallenges().stream()
                        .filter(it -> it.getTargetMenu() != null && !it.getTargetMenu().isBlank())
                        .peek(it -> {
                            it.setTargetMenu(it.getTargetMenu().trim());
                            if (it.getTitle() != null) it.setTitle(it.getTitle().trim());
                            if (it.getDescription() != null) it.setDescription(it.getDescription().trim());
                        })
                        .filter(it -> allowed.contains(it.getTargetMenu()))
                        .filter(it -> used.add(it.getTargetMenu()))
                        .limit(3)
                        .forEach(it -> list.add(Challenges.builder()
                                .user(user)
                                .title(nvl(it.getTitle(), it.getTargetMenu() + " 먹기"))
                                .targetMenuName(it.getTargetMenu())
                                .description(nvl(it.getDescription(), "가볍게 도전!"))
                                .startDate(weekStart)
                                .endDate(weekEnd)
                                .status(ChallengeStatus.PENDING)
                                .isAvoidType(false)
                                .build()));
            }

            // 모자라면 allowed에서 보충
            var usedMenus = new java.util.HashSet<String>();
            for (Challenges c : list) usedMenus.add(c.getTargetMenuName());
            for (String m : allowed) {
                if (list.size() >= 3) break;
                if (m == null || m.isBlank() || usedMenus.contains(m)) continue;
                list.add(Challenges.builder()
                        .user(user)
                        .title(m + " 먹기")
                        .targetMenuName(m)
                        .description("가볍게 도전!")
                        .startDate(weekStart)
                        .endDate(weekEnd)
                        .status(ChallengeStatus.PENDING)
                        .isAvoidType(false)
                        .build());
                usedMenus.add(m);
            }

            // 그래도 부족하면(allowed가 너무 짧을 때) 반복 채움
            while (list.size() < 3) {
                String m = allowed.get(list.size() % allowed.size());
                list.add(Challenges.builder()
                        .user(user)
                        .title(m + " 먹기")
                        .targetMenuName(m)
                        .description("가볍게 도전!")
                        .startDate(weekStart)
                        .endDate(weekEnd)
                        .status(ChallengeStatus.PENDING)
                        .isAvoidType(false)
                        .build());
            }

            return list;

        } catch (Exception e) {
            log.warn("LLM 생성 실패. 허용 목록으로 대체: {}", e.toString());
            List<String> shuffled = new ArrayList<>(allowed);
            Collections.shuffle(shuffled);
            for (int i = 0; i < 3; i++) {
                String m = shuffled.get(i % shuffled.size());
                list.add(Challenges.builder()
                        .user(user)
                        .title(m + " 먹기")
                        .targetMenuName(m)
                        .description("가볍게 도전!")
                        .startDate(weekStart)
                        .endDate(weekEnd)
                        .status(ChallengeStatus.PENDING)
                        .isAvoidType(false)
                        .build());
            }
            return list;
        }
    }


    private static String nvl(String s, String def) { return (s == null || s.isBlank()) ? def : s; }
}
