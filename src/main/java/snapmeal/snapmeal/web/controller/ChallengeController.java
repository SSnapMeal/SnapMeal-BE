package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.global.util.AuthService;
import snapmeal.snapmeal.service.ChallengeGeneratorService;
import snapmeal.snapmeal.service.ChallengeService;
import snapmeal.snapmeal.web.dto.ChallengeDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final ChallengeGeneratorService generatorService;
    private final AuthService authService;

    @Operation(summary = "내 챌린지 목록 조회")
    @GetMapping("/my")
    public List<ChallengeDto.Response> listMine(
            @RequestParam(required = false, defaultValue = "IN_PROGRESS,SUCCESS,PENDING") String statuses
    ) {
        return challengeService.listMine(statuses).stream().map(this::toDto).toList();
    }

    @Operation(summary = "챌린지 참여하기")
    @ApiResponse(responseCode = "200", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{ \"status\": \"IN_PROGRESS\" }"),
            schema = @Schema(implementation = ChallengeDto.ParticipateResponse.class)
    ))
    @PostMapping("/{challengeId}/participate")
    public ChallengeDto.ParticipateResponse participate(@PathVariable Long challengeId) {
        return challengeService.participate(challengeId);
    }

    @Operation(summary = "챌린지 포기하기")
    @PostMapping("/{challengeId}/give-up")
    public ChallengeDto.ParticipateResponse giveUp(@PathVariable Long challengeId) {
        return challengeService.giveUp(challengeId);
    }

    // (테스트용) 이번 주 3개 생성 — 현재 로그인 사용자 기준
    @PostMapping("/weekly/generate")
    public List<ChallengeDto.Response> generateWeeklyForMe() {
        var user = authService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        return generatorService.generateWeeklyForUser(user, weekStart, weekEnd)
                .stream().map(this::toDto).toList();
    }

    private ChallengeDto.Response toDto(Challenges c) {
        return ChallengeDto.Response.builder()
                .challengeId(c.getChallengeId())
                .title(c.getTitle())
                .targetMenuName(c.getTargetMenuName())
                .description(c.getDescription())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus().name())
                .participatedAt(c.getParticipatedAt())
                .completedAt(c.getCompletedAt())
                .cancelledAt(c.getCancelledAt())
                .build();
    }
}
