package snapmeal.snapmeal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionRequestDto {

    @Schema(description = "S3에 업로드된 이미지 URL", example = "https://bucket.s3.amazonaws.com/image.jpg")
    @JsonProperty("image_url")
    private String imageUrl;
}
