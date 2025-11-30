package snapmeal.snapmeal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ImageDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadResponse {

        @Schema(example = "1", description = "저장된 이미지의 ID")
        private Long imageId;

        @Schema(
                example = "https://snapmeal-bucket.s3.ap-northeast-2.amazonaws.com/1234-abc-image.jpg",
                description = "S3에 업로드된 이미지 URL"
        )
        private String imageUrl;
    }
}