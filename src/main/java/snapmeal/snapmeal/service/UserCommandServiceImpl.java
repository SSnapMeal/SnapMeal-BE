package snapmeal.snapmeal.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import snapmeal.snapmeal.config.security.JwtTokenProvider;
import snapmeal.snapmeal.converter.UserConverter;
import snapmeal.snapmeal.domain.Challenges;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.domain.enums.Role;
import snapmeal.snapmeal.global.code.ErrorCode;
import snapmeal.snapmeal.global.handler.GeneralException;
import snapmeal.snapmeal.global.handler.TokenHandler;
import snapmeal.snapmeal.global.handler.UserHandler;
import snapmeal.snapmeal.repository.BlacklistRepository;
import snapmeal.snapmeal.repository.ChallengeRepository;
import snapmeal.snapmeal.repository.ChallengeReviewRepository;
import snapmeal.snapmeal.repository.MealsRepository;
import snapmeal.snapmeal.repository.NutritionAnalysisRepository;
import snapmeal.snapmeal.repository.RefreshTokenRepository;
import snapmeal.snapmeal.repository.UserRepository;
import snapmeal.snapmeal.repository.WeeklyReportRepository;
import snapmeal.snapmeal.web.dto.DietTypeRequestDto;
import snapmeal.snapmeal.web.dto.TokenServiceResponse;
import snapmeal.snapmeal.web.dto.UserRequestDto;
import snapmeal.snapmeal.web.dto.UserResponseDto;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;
    private final S3UploadService s3UploadService;
    private final PasswordEncoder passwordEncoder;
    private final DietTypeService dietTypeService;
    private final ChallengeRepository challengeRepository;
    private final ChallengeReviewRepository challengeReviewRepository;
    private final MealsRepository mealsRepository;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final WeeklyReportRepository weeklyReportRepository;


    @Override
    @Transactional
    public UserResponseDto.UserDto joinUser(UserRequestDto.JoinDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // 인증 정보에서 이메일 추출

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    updateUserData(existingUser, request);
                    return existingUser;
                })
                .orElseGet(() -> {
                    String tag = dietTypeService.analyzeDietType(
                            new DietTypeRequestDto(request.getSelectedDietTypes())
                    ).getDietType();

                    request.setType(tag);
                    User newUser = UserConverter.toUser(request,passwordEncoder);
                    return userRepository.save(newUser);
                });

        return UserConverter.toUserSignUpResponseDto(user);
    }



    @Override
    public UserResponseDto.LoginDto saveNewUser(UserRequestDto.JoinDto request) {


        User user = UserConverter.toUser(request, passwordEncoder);
        userRepository.save(user);

        TokenServiceResponse token = jwtTokenProvider.createToken(user);

        return UserResponseDto.LoginDto.builder()
                .tokenServiceResponse(token)
                .isNewUser(true)
                .build();
    }




    @Override
    public UserResponseDto.LoginDto isnewUser(String email) {
        // 이메일로 기존 유저 조회
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            TokenServiceResponse token = jwtTokenProvider.createToken(user);

                return UserResponseDto.LoginDto.builder()
                        .tokenServiceResponse(token)
                        .isNewUser(false)
                        .build();


        }
        // 신규 유저 생성
        User newUser = User.builder()
                .email(email)
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
        TokenServiceResponse token = jwtTokenProvider.createToken(newUser);

        return UserResponseDto.LoginDto.builder()
                .tokenServiceResponse(token)
                .isNewUser(true)
                .build();
    }
    public UserResponseDto.LoginDto signIn(UserRequestDto.SignInRequestDto request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new UserHandler(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserHandler(ErrorCode.INVALID_PASSWORD);
        }

        TokenServiceResponse token = jwtTokenProvider.createToken(user);
        return UserResponseDto.LoginDto.builder()
                .tokenServiceResponse(token)
                .isNewUser(false)
                .build();
    }

    private void updateUserData(User user, UserRequestDto.JoinDto request) {


        user.updateAll(request);
    }



    @Override
    public void logout(String accessToken, String refreshToken) {
        String token = extractAndValidateToken(accessToken);
        validateTokenOwnership(token);
        invalidateRefreshToken(refreshToken);
        blacklistAccessToken(token);
    }

    private String extractAndValidateToken(String accessToken) {
        if (isInvalidTokenFormat(accessToken)) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN);
        }
        return accessToken.replace("Bearer ", "");
    }

    private boolean isInvalidTokenFormat(String token) {
        return token == null || !token.startsWith("Bearer ");
    }

    private void validateTokenOwnership(String token) {
        jwtTokenProvider.getEmailFromToken(token);
    }

    private void invalidateRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteToken(refreshToken);
    }

    private void blacklistAccessToken(String token) {
        long expiration = jwtTokenProvider.getExpiration(token);
        blacklistRepository.addToBlacklist(token, expiration);
    }

    @Transactional
    @Override
    public void deleteUser(String accessToken, String refreshToken) {

        // 1. Access Token → 이메일 추출
        String token = accessToken.replace("Bearer ", "");
        String email = jwtTokenProvider.getEmailFromToken(token);

        // 2. 이메일로 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorCode.USER_NOT_FOUND));

        // 3. 토큰 삭제 (블랙리스트 + 리프레시 지우기)
        deleteTokensForUser(accessToken, refreshToken);

        // 4. 유저가 생성한 챌린지 + 리뷰 삭제
        List<Challenges> challengeList = challengeRepository.findAllByUser(user);

        for (Challenges challenge : challengeList) {
            challengeReviewRepository.deleteAllByChallenge(challenge);
        }

        challengeRepository.deleteAllByUser(user);

        // 5. 이미지 삭제
        s3UploadService.deleteAllImagesByUser(user);

        // 6. 식단 삭제
        mealsRepository.deleteAllByUser(user);

        // 7. 건강 분석 삭제
        nutritionAnalysisRepository.deleteAllByUser(user);

        // 8. 주간 리포트 삭제
        weeklyReportRepository.deleteAllByUser(user);

        // 9. 마지막에 유저 삭제
        userRepository.delete(user);
    }


    private void deleteTokensForUser(String accessToken, String refreshToken) {

        if (accessToken == null || refreshToken == null) {
            throw new TokenHandler(ErrorCode.INVALID_TOKEN);
        }

        try {
            String pureAccessToken = accessToken.replace("Bearer ", "");

            long expiration = jwtTokenProvider.getExpiration(pureAccessToken);

            // 1. 블랙리스트 등록
            blacklistRepository.addToBlacklist(pureAccessToken, expiration);

            // 2. refresh 삭제
            refreshTokenRepository.deleteToken(refreshToken);

        } catch (Exception e) {
            throw new GeneralException(ErrorCode.INVALID_TOKEN);
        }
    }
}

