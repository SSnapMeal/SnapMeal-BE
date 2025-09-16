package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import snapmeal.snapmeal.service.S3UploadService;
import snapmeal.snapmeal.web.dto.PredictionResponseDto;

import java.io.IOException;

@RestController
@RequestMapping("/images")
@Tag(name = "Image API", description = "이미지 업로드 API")
@RequiredArgsConstructor
public class S3Controller {

    private final S3UploadService s3UploadService;

    @Operation(
            summary = "이미지 업로드 및 예측",
            description = "이미지를 S3에 업로드하고 DB에 URL, user_id를 DB에 저장한 뒤, 업로드된 image_id와 예측된 class_name을 반환합니다.",
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
        // S3 업로드 + 예측
        PredictionResponseDto predictionResult = s3UploadService.uploadPredictAndSave(file);

        return ResponseEntity.ok(predictionResult);
    }
}