package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.swagger.ApiErrorCodeExamples;
import snapmeal.snapmeal.service.KakaoService;
import snapmeal.snapmeal.service.UserCommandService;
import snapmeal.snapmeal.web.dto.KakaoUserInfoResponseDto;
import snapmeal.snapmeal.web.dto.UserRequestDto;
import snapmeal.snapmeal.web.dto.UserResponseDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "유저 관리 API")
public class UserController {

    private final UserCommandService userCommandService;
    private final KakaoService kakaoService;


    @Operation(
            summary = "카카오 로그인 콜백",
            description = "프론트에서 받은 인가 코드(code)를 이용해 카카오 AccessToken을 발급받고, 사용자 정보를 조회하여 회원가입 또는 로그인을 처리합니다."
    )
    @ApiErrorCodeExamples(
            ErrorCode.BAD_REQUEST
    )
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<UserResponseDto.LoginDto> kakaoCallback(
            @io.swagger.v3.oas.annotations.Parameter(description = "인가 코드")
            @RequestParam("code") String code) {

        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto kakaoUser = kakaoService.getUserInfo(accessToken);
        String email = kakaoUser.getKakaoAccount().getEmail();
        UserResponseDto.LoginDto response = userCommandService.isnewUser(email);

        String jwtToken = response.getTokenServiceResponse().getAccessToken();
        String encodedToken = UriUtils.encode(jwtToken, StandardCharsets.UTF_8);

        URI redirectUri = URI.create("snapmeal://home?token=" + encodedToken);
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @PostMapping("/sign-up")
    @Operation(
            summary = "회원가입"
    )
    @ApiErrorCodeExamples(
            ErrorCode.BAD_REQUEST
    )
    public ResponseEntity<UserResponseDto.UserDto> signup(@RequestBody UserRequestDto.JoinDto joinDto) {
        UserResponseDto.UserDto response = userCommandService.joinUser(joinDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "일반 로그인"
    )
    @ApiErrorCodeExamples({
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INVALID_PASSWORD
    })

    public ResponseEntity<UserResponseDto.LoginDto> singIn(
            @RequestBody UserRequestDto.SignInRequestDto signInRequestDto) {
        UserResponseDto.LoginDto response = userCommandService.signIn(signInRequestDto);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"message\": \"로그아웃 성공\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 토큰",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "isSuccess": false,
                                  "code": "AUTH005",
                                  "message": "유효하지 않은 토큰입니다.",
                                  "status": 401
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "isSuccess": false,
                                  "code": "AUTH002",
                                  "message": "사용자를 찾을 수 없습니다.",
                                  "status": 404
                                }
                                """))
            )
    })
    @Operation(
            summary = "로그아웃",
            description = """
                사용자의 Access Token·Refresh Token을 무효화합니다.

                ✔ Access Token  
                - Bearer {token} 형태로 전달  
                - 유효성 검사 후 사용자 식별  
                - 블랙리스트에 등록되어 재사용을 차단합니다.

                ✔ Refresh Token  
                - Redis에서 즉시 삭제  
                - 재발급 불가 상태로 만듭니다.

                로그아웃 후 토큰은 더 이상 사용될 수 없습니다.
                """
    )
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_TOKEN,
            ErrorCode.USER_NOT_FOUND
    })
    public ResponseEntity<Map<String, String>> logout(
            @Parameter(description = "Bearer Access Token", required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String accessToken,

            @Parameter(description = "Refresh Token", required = true,
                    example = "eyJhbGciOiJIUzI1NiIsInR5...")
            @RequestHeader("RefreshToken") String refreshToken
    ) {
        userCommandService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @DeleteMapping("/withdraw")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원탈퇴 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                { "message": "회원탈퇴 완료" }
                                """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 토큰",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "isSuccess": false,
                                  "code": "AUTH005",
                                  "message": "유효하지 않은 토큰입니다.",
                                  "status": 401
                                }
                                """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "isSuccess": false,
                                  "code": "AUTH002",
                                  "message": "사용자를 찾을 수 없습니다.",
                                  "status": 404
                                }
                                """))
            )
    })
    @Operation(
            summary = "회원 탈퇴",
            description = """
                로그인된 사용자의 계정을 완전히 삭제합니다.

                ⬥ Authorization (Access Token)
                - Bearer {token} 형태로 전달해야 합니다.
                - 토큰에서 사용자 이메일/ID를 추출하여 탈퇴할 유저를 식별합니다.

                ⬥ RefreshToken
                - Redis 저장 Refresh Token 을 즉시 삭제하여 재로그인이 불가능해집니다.

                탈퇴 시 삭제되는 정보:
                - 유저 기본 계정 정보
                - 챌린지 및 리뷰
                - 식단 이미지 (S3 포함)
                - 식사 기록(Meals)
                - 영양 분석 기록
                - 주간 리포트
                - Refresh Token 삭제
                - Access Token 블랙리스트 등록

                ➤ 응답 바디는 없이 200 OK 만 반환됩니다.
                """
    )
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_TOKEN,
            ErrorCode.USER_NOT_FOUND
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Bearer Access Token", required = true, example = "Bearer eyJhbG...")
            @RequestHeader("Authorization") String accessToken,

            @Parameter(description = "Refresh Token", required = true, example = "eyJhbGciOi...")
            @RequestHeader("RefreshToken") String refreshToken
    ) {
        userCommandService.deleteUser(accessToken, refreshToken);
        return ResponseEntity.ok().build();
    }

}


