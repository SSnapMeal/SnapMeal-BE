package snapmeal.snapmeal.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.WeeklyReportDetails;
import snapmeal.snapmeal.domain.WeeklyReports;
import snapmeal.snapmeal.global.OpenAiClient;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.repository.UserRepository;
import snapmeal.snapmeal.repository.WeeklyReportRepository;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.web.dto.WeeklyReportAiResponse;
import snapmeal.snapmeal.web.dto.WeeklyReportDetailsDto;
import snapmeal.snapmeal.web.dto.WeeklyReportResponseDto;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final OpenAiClient openAiClient;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public void generateWeeklyReports() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
//                LocalDate today = LocalDate.now();
//                LocalDate weekStart = today.with(DayOfWeek.MONDAY);
//                LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

                LocalDate weekStart = LocalDate.of(2025, 7, 14);
                LocalDate weekEnd = LocalDate.now();
                List<NutritionAnalysis> analyses = nutritionAnalysisRepository
                        .findAllByUserAndCreatedAtBetween(
                                user,
                                weekStart.atStartOfDay(),
                                weekEnd.atTime(LocalTime.MAX)
                        );

                if (analyses.isEmpty()) continue;

                float totalCal = 0, totalProtein = 0, totalFat = 0, totalCarb = 0;
                for (NutritionAnalysis na : analyses) {
                    totalCal += na.getCalories();
                    totalProtein += na.getProtein();
                    totalFat += na.getFat();
                    totalCarb += na.getCarbs();
                }

                String prompt = generateAiPrompt(analyses, totalCal, totalCarb);
                String aiResponse = openAiClient.requestCompletion("당신은 건강 관리 전문가입니다.", prompt);
                WeeklyReportAiResponse parsed = parseAiResponse(aiResponse);

                WeeklyReports report = WeeklyReports.builder()
                        .user(user)
                        .reportDate(weekStart)
                        .totalCalories(totalCal)
                        .totalProtein(totalProtein)
                        .totalFat(totalFat)
                        .totalCarbs(totalCarb)
                        .nutritionSummary(parsed.getNutritionSummary())
                        .caloriePattern(parsed.getCaloriePattern())
                        .healthGuidance(parsed.getHealthGuidance())
                        .build();

                weeklyReportRepository.save(report);

            } catch (IOException e) {
                log.error("OpenAI 호출 실패 - 사용자: {}", user.getEmail(), e);
                // 사용자별로 실패해도 다른 사용자 리포트는 계속 생성되게 처리
            }
        }
    }

    private String generateAiPrompt(List<NutritionAnalysis> list, float totalCal, float totalCarb) {
        return String.format("""
            아래는 사용자의 일주일간 식단 정보입니다.
            총 칼로리: %.1f kcal, 총 탄수화물: %.1f g
            하루별 섭취 기록:
            %s

            아래 세 항목을 각각 문단으로 설명하고 JSON으로 응답해주세요:
            1. 영양소 섭취 경향 요약
            2. 저녁 시간대 위주의 섭취 경향 및 수치 요약
            3. 식사 및 생활 가이드

            응답 예시:
            {
              "nutritionSummary": "...",
              "caloriePattern": "...",
              "healthGuidance": "..."
            }
            """,
                totalCal, totalCarb,
                list.stream()
                        .map(n -> "- " + n.getFoodNames() + ": " + n.getCalories() + "kcal")
                        .collect(Collectors.joining("\n"))
        );
    }

    private WeeklyReportAiResponse parseAiResponse(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, WeeklyReportAiResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답 파싱 실패", e);
        }
    }




    @Transactional(readOnly = true)
    public WeeklyReportResponseDto getMyWeeklyReport() {
        User user = authService.getCurrentUser();

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        WeeklyReports report = weeklyReportRepository
                .findByUserAndReportDate(user, weekStart)
                .orElseThrow(() -> new NotFoundException("이번 주 리포트가 아직 생성되지 않았습니다."));

        return new WeeklyReportResponseDto(report);
    }

}
