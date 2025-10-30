package snapmeal.snapmeal.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snapmeal.snapmeal.converter.WeeklyReportConverter;
import snapmeal.snapmeal.domain.NutritionAnalysis;
import snapmeal.snapmeal.domain.WeeklyReportDetails;
import snapmeal.snapmeal.domain.WeeklyReports;
import snapmeal.snapmeal.global.OpenAiClient;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.handler.ReportHandler;
import snapmeal.snapmeal.global.handler.UserHandler;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.global.util.WeeklyReportPromptBuilder;
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
    private final WeeklyReportPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final WeeklyReportConverter weeklyReportConverter;

    @Transactional
    public void generateWeeklyReports() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                //                LocalDate today = LocalDate.now();
//                LocalDate weekStart = today.with(DayOfWeek.MONDAY);
//                LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
                LocalDate weekStart = LocalDate.of(2025, 9, 15); // TODO: 운영 시 LocalDate.now().with(DayOfWeek.MONDAY)
                LocalDate weekEnd = LocalDate.now();

                List<NutritionAnalysis> analyses =
                        nutritionAnalysisRepository.findAllByUserAndCreatedAtBetween(
                                user,
                                weekStart.atStartOfDay(),
                                weekEnd.atTime(LocalTime.MAX)
                        );

                if (analyses.isEmpty()) continue;

                // 총합 계산
                float totalCal = 0, totalProtein = 0, totalSugar = 0, totalFat = 0, totalCarb = 0;
                for (NutritionAnalysis na : analyses) {
                    totalCal += na.getCalories();
                    totalSugar += na.getSugar();
                    totalProtein += na.getProtein();
                    totalFat += na.getFat();
                    totalCarb += na.getCarbs();
                }

                // AI 호출
                String prompt = promptBuilder.buildWeeklyReportPrompt(analyses, totalCal, totalCarb);
                String aiResponse = openAiClient.requestCompletion("당신은 건강 관리 전문가입니다.", prompt);

                // 응답 파싱
                WeeklyReportAiResponse parsed = objectMapper.readValue(aiResponse, WeeklyReportAiResponse.class);

                // JSON 직렬화
                String caloriePatternJson = objectMapper.writeValueAsString(parsed.getCaloriePattern());
                String healthGuidanceJson = objectMapper.writeValueAsString(parsed.getHealthGuidance());


                WeeklyReports report = WeeklyReports.builder()
                        .user(user)
                        .reportDate(weekStart)
                        .totalCalories(totalCal)
                        .totalProtein(totalProtein)
                        .totalSugar(totalSugar)
                        .totalFat(totalFat)
                        .totalCarbs(totalCarb)
                        .nutritionSummary(parsed.getNutritionSummary())
                        .caloriePattern(caloriePatternJson)
                        .healthGuidance(healthGuidanceJson)
                        .build();

                weeklyReportRepository.save(report);

            } catch (Exception e) {
                log.error("OpenAI 리포트 생성 실패 - 사용자: {}", user.getEmail(), e);
                throw new ReportHandler(ErrorCode.AI_RESPONSE_ERROR);
            }

        }
    }
    @Transactional(readOnly = true)
    public WeeklyReportResponseDto getWeeklyReportByWeekStart(LocalDate weekStart) {
        User user = authService.getCurrentUser();

        //weekStart가 null이면 저번 주 월요일로 기본값 설정
        if (weekStart == null) {
            weekStart = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        }

        WeeklyReports report = weeklyReportRepository
                .findByUserAndReportDate(user, weekStart)
                .orElseThrow(() -> new ReportHandler(ErrorCode.REPORT_NOT_FOUND));

        return weeklyReportConverter.toDto(report);
    }


}
