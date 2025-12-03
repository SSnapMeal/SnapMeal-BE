package snapmeal.snapmeal.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import snapmeal.snapmeal.domain.Images;
import snapmeal.snapmeal.service.NutritionAnalysisService;
import snapmeal.snapmeal.service.NutritionParsingService;
import snapmeal.snapmeal.service.OpenAiVisionService;
import snapmeal.snapmeal.service.S3UploadService;
import snapmeal.snapmeal.web.dto.NutritionOcrResponseDto;
import snapmeal.snapmeal.web.dto.NutritionRequestDto;
@RestController
@RequestMapping("/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionAnalysisService nutritionAnalysisService;
    private final S3UploadService s3UploadService;  // 이미 이미지 업로드용 서비스 있을 거라고 가정

    @PostMapping(value = "/ocr", consumes = "multipart/form-data")
    public ResponseEntity<NutritionOcrResponseDto> analyzeNutrition(
            @RequestPart("file") MultipartFile file
    ) {
        // 1) 먼저 이미지를 S3에 업로드하고 Images 엔티티를 만든다고 가정
        Images image = s3UploadService.uploadAndSaveOnly(file);

        // 2) OCR + DB 저장 + nutritionId 포함 DTO 반환
        NutritionOcrResponseDto response = nutritionAnalysisService.analyzeAndSave(file, image);

        // 3) 프론트로 nutritionId 포함해서 응답
        return ResponseEntity.ok(response);
    }
}

