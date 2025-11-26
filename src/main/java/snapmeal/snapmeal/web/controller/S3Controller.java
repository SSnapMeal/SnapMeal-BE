package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import snapmeal.snapmeal.service.S3UploadService;
import snapmeal.snapmeal.web.dto.ImageDto;
import snapmeal.snapmeal.web.dto.PredictionResponseDto;
import snapmeal.snapmeal.domain.Images;

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

    // 예측 없이 "이미지만 업로드" 하는 엔드포인트
    @Operation(
            summary = "이미지 업로드 (예측 없이)",
            description = "이미지를 S3에 업로드하고 DB에 저장만 합니다. 예측 서버는 호출하지 않습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "업로드 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ImageDto.UploadResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (파일 크기 초과 등)",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping(
            value = "/upload-only",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImageDto.UploadResponse> uploadImageOnly(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        Images saved = s3UploadService.uploadAndSaveOnly(file);

        ImageDto.UploadResponse response = ImageDto.UploadResponse.builder()
                .imageId(saved.getImgId())
                .imageUrl(saved.getImageUrl())
                .build();

        return ResponseEntity.ok(response);
    }
}