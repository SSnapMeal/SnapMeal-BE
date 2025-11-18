package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            @RequestParam(required = false, defaultValue = "IN_PROGRESS,SUCCESS,PENDING,FAIL") String statuses
    ) {
        // 서비스에서 stamps/satisfiedDays/introduction/participation까지 채워진 DTO 리턴
        return challengeService.listMineWithStamps(statuses);
    }

    @Operation(summary = "챌린지 참여하기")
    @ApiResponse(responseCode = "200", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ChallengeDto.Response.ParticipateResponse.class),
            examples = @ExampleObject(value = "{ \"status\": \"IN_PROGRESS\" }")
    ))
    @PostMapping("/{challengeId}/participate")
    public ChallengeDto.Response.ParticipateResponse participate(@PathVariable Long challengeId) {
        return challengeService.participate(challengeId);
    }

    @Operation(summary = "챌린지 포기하기")
    @ApiResponse(responseCode = "200", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ChallengeDto.Response.ParticipateResponse.class),
            examples = @ExampleObject(value = "{ \"status\": \"CANCELLED\" }")
    ))
    @PostMapping("/{challengeId}/give-up")
    public ChallengeDto.Response.ParticipateResponse giveUp(@PathVariable Long challengeId) {
        return challengeService.giveUp(challengeId);
    }

    @Operation(
            summary = "챌린지 상세조회",
            description = """
        사용자가 선택한 챌린지의 상세 정보를 반환합니다.  
        반환 데이터에는 제목, 커버 이미지, 목표, 상세 설명, 기간, 상태, 참여 여부 등이 포함됩니다.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 조회됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChallengeDto.Response.class),
                            examples = @ExampleObject(value =
                                    "{\n" +
                                            "  \"challengeId\": 1,\n" +
                                            "  \"title\": \"커피 마시지 않기\",\n" +
                                            "  \"coverImageUrl\": \"https://cdn.snapmeal.app/challenges/coffee-off.png\",\n" +
                                            "  \"targetMenuName\": \"커피\",\n" +
                                            "  \"description\": \"아메리카노, 에스프레소, 라떼 등 모든 커피 종류 포함\",\n" +
                                            "  \"status\": \"PENDING\",\n" +
                                            "  \"startDate\": \"2025-10-20\",\n" +
                                            "  \"endDate\": \"2025-10-26\",\n" +
                                            "  \"introduction\": {\n" +
                                            "    \"mainGoal\": \"커피 안마시기\",\n" +
                                            "    \"purpose\": \"카페인 줄이기 및 건강 관리\",\n" +
                                            "    \"detailDescription\": \"아메리카노, 에스프레소, 라떼 등 모든 커피 종류 포함\",\n" +
                                            "    \"weeklyTarget\": \"주 5회 이상\",\n" +
                                            "    \"successCondition\": \"기간 동안 커피 관련 미기록시 성공\"\n" +
                                            "  },\n" +
                                            "  \"participation\": {\n" +
                                            "    \"isParticipating\": false,\n" +
                                            "    \"participatedAt\": null\n" +
                                            "  }\n" +
                                            "}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 챌린지를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{challengeId}")
    public ChallengeDto.Response getChallengeDetail(@PathVariable Long challengeId) {
        var currentUser = authService.getCurrentUser();
        return challengeService.getDetail(challengeId, currentUser.getId());
    }


    // 참여 전 후보(PENDING) 목록: 스탬프 없음
    @Operation(
            summary = "참여 전 챌린지 목록(PENDING): 스탬프 미포함",
            description = "사용자가 참여 전인 챌린지 목록을 보여줍니다."
    )
    @GetMapping("/my/available")
    public List<ChallengeDto.Response> listMyAvailableAll() {
        return challengeService.listAvailableAll();
    }

    // 참여 중 목록: 스탬프 포함
    @Operation(
            summary = "참여 중 챌린지 목록: 스탬프 포함",
            description = "사용자가 참여중인 챌린지 목록을 보여줍니다."
    )
    @GetMapping("/my/participating")
    public List<ChallengeDto.Response> listMyParticipatingWithStamps() {
        return challengeService.listParticipatingWithStamps();
    }

    // 챌린지 전체 목록 조회
    @Operation(
            summary = "현재 생성되어 있는 모든 챌린지 조회",
            description = "전체 챌린지 목록을 조회합니다."
    )
    @GetMapping("/all")
    public List<ChallengeDto.Response> listCurrentGeneratedChallenges() {
        return challengeService.listCurrentGeneratedChallenges();
    }

    // (테스트용) 이번 주 3개 생성 — 현재 로그인 사용자 기준
    @PostMapping("/weekly/generate")
    @Operation(
            summary = "챌린지 생성",
            description = "한 주에 3개의 챌린지를 생성합니다."
    )
    public List<ChallengeDto.Response> generateWeeklyForMe(@RequestParam(defaultValue = "false") boolean force) {
        var user = authService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        var created = generatorService.generateWeeklyForUser(user, weekStart, weekEnd, force);

        // 각 챌린지에 대해 서비스의 getDetail을 호출해서
        // stamps / satisfiedDays / introduction / participation 이 꽉 찬 DTO로 반환
        return created.stream()
                .map(c -> challengeService.getDetail(c.getChallengeId(), user.getId()))
                .toList();
    }

    /* ==========================
     * 리뷰 API
     * ========================== */

    @Operation(
            summary = "챌린지 리뷰 작성",
            description = """
            챌린지가 종료된 이후에만 별점(0~5점)과 텍스트 리뷰를 작성할 수 있습니다.
            - rating: 0 ~ 5 사이 정수 (필수)
            - content: 후기 내용 (선택)
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "리뷰 작성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChallengeDto.Response.ReviewResponse.class),
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"reviewId\": 10,\n" +
                                            "  \"challengeId\": 1,\n" +
                                            "  \"rating\": 5,\n" +
                                            "  \"content\": \"일주일 동안 커피 끊으니까 잠도 잘 오고 좋았어요!\",\n" +
                                            "  \"createdAt\": \"2025-11-18T10:30:00\",\n" +
                                            "  \"updatedAt\": \"2025-11-18T10:30:00\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "별점 범위(0~5) 위반 또는 잘못된 요청",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "챌린지를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "챌린지가 아직 종료되지 않아 리뷰 작성 불가",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/{challengeId}/reviews")
    public ChallengeDto.Response.ReviewResponse createReview(
            @PathVariable Long challengeId,
            @RequestBody ChallengeDto.Response.ReviewCreateOrUpdateRequest request
    ) {
        // Service의 createReview 그대로 호출
        return challengeService.createReview(challengeId, request);
    }
    @Operation(
            summary = "챌린지 리뷰 수정",
            description = """
            본인이 작성한 리뷰만 수정할 수 있습니다.
            - rating: 0 ~ 5 사이 정수 (null 이면 수정하지 않음)
            - content: 문자열 (null 이면 수정하지 않음)
            둘 중 하나만 보내도 되고, 둘 다 보내도 됩니다.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "리뷰 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChallengeDto.Response.ReviewResponse.class),
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"reviewId\": 10,\n" +
                                            "  \"challengeId\": 1,\n" +
                                            "  \"rating\": 4,\n" +
                                            "  \"content\": \"생각보다 힘들었지만 의미 있었어요.\",\n" +
                                            "  \"createdAt\": \"2025-11-18T10:30:00\",\n" +
                                            "  \"updatedAt\": \"2025-11-18T11:00:00\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "별점 범위(0~5) 위반",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성한 리뷰가 아님",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PatchMapping("/reviews/{reviewId}")
    public ChallengeDto.Response.ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @RequestBody ChallengeDto.Response.ReviewCreateOrUpdateRequest request
    ) {
        return challengeService.updateReview(reviewId, request);
    }
    @Operation(
            summary = "챌린지 리뷰 삭제",
            description = "본인이 작성한 리뷰만 삭제할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "리뷰 삭제 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성한 리뷰가 아님",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        challengeService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}