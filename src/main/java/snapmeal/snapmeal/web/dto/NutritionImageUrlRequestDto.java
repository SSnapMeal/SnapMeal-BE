package snapmeal.snapmeal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "NutritionImageUrlRequestDto", description = "영양성분표 이미지 URL로 분석 요청할 때 사용하는 DTO")
public class NutritionImageUrlRequestDto {

    @Schema(
            description = "영양성분표 이미지가 저장된 URL (S3 등)",
            example = "https://snapmeal-s3-bucket.s3.ap-northeast-2.amazonaws.com/images/nutrition-label-123.jpg"
    )
    private String imageUrl;
}
