package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import snapmeal.snapmeal.domain.User;
import snapmeal.snapmeal.repository.UserRepository;
import snapmeal.snapmeal.service.S3UploadService;
import snapmeal.snapmeal.web.dto.PredictionResponseDto;

import java.io.IOException;

@RestController
@RequestMapping("/images")
@Tag(name = "Image API", description = "이미지 업로드 API")
@RequiredArgsConstructor
public class S3Controller {

    private final S3UploadService s3UploadService;
    private final UserRepository userRepository;


    @Operation(
            summary = "이미지 업로드 및 예측",
            description = "이미지를 S3에 업로드하고 DB에 URL, user_id를 DB에 저장한 뒤, 예측된 class_id를 반환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "multipart/form-data"
                    )
            )
    )
    @PostMapping(value = "/upload-predict", consumes = "multipart/form-data")
    public ResponseEntity<PredictionResponseDto> uploadImage(
            @RequestPart MultipartFile file
    ) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // 로그인한 사용자의 이메일

        // 이메일 기준 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // S3 업로드 + 예측
        PredictionResponseDto predictionResult = s3UploadService.uploadPredictAndSave(file, user);

        return ResponseEntity.ok(predictionResult);
    }
}