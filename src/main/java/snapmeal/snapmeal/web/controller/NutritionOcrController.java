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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import snapmeal.snapmeal.service.NutritionParsingService;
import snapmeal.snapmeal.service.OpenAiVisionService;
import snapmeal.snapmeal.web.dto.NutritionRequestDto;

@RestController
@RequestMapping("/nutrition-ocr")
@RequiredArgsConstructor
public class NutritionOcrController {

    private final OpenAiVisionService visionService;
    private final NutritionParsingService parsingService;

    @Operation(
            summary = "영양성분표 이미지 파일 업로드로 영양 성분 분석",
            description = """
                    Swagger에서 이미지를 직접 업로드하면,
                    OpenAI Vision으로 영양성분표를 OCR + 분석해서
                    칼로리/탄수화물/단백질/당류/지방/나트륨 을 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NutritionRequestDto.TotalNutritionRequestDto.class),
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"calories\": 450,\n" +
                                            "  \"protein\": 20.5,\n" +
                                            "  \"carbs\": 60.0,\n" +
                                            "  \"sugar\": 10.0,\n" +
                                            "  \"fat\": 15.0,\n" +
                                            "  \"sodium\": 35.0,\n" +
                                            "  \"nutritionId\": null\n" +
                                            "}"
                            )
                    )
            )
    })
    @PostMapping(
            value = "/from-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE   // multipart 설정
    )
    public NutritionRequestDto.TotalNutritionRequestDto analyzeFromFile(
            @Parameter(
                    description = "영양성분표 이미지 파일 (jpg, png 등)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file
    ) {
        // 파일 → OpenAI Vision 호출 (Base64 data URL 방식)
        String nutritionJson = visionService.requestNutritionJsonFromFile(file);

        // JSON → DTO 변환
        NutritionRequestDto.TotalNutritionRequestDto dto =
                parsingService.parseToTotalNutrition(nutritionJson);

        // DTO 반환
        return dto;
    }
}
