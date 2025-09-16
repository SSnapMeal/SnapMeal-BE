package snapmeal.snapmeal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectionDto {

    @Schema(description = "예측된 이미지 클래스 번호", example = "2")
    @JsonProperty("class_id")
    private int classId;

    @Schema(description = "예측된 음식 클래스 이름", example = "간장게장")
    @JsonProperty("class_name")
    private String className;

    @Schema(description = "예측 음식 정확도", example = "0.876988")
    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("box")
    private BoundingBoxDto box;

    // box 안에 들어가는 값들을 위한 내부 클래스
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoundingBoxDto {
        private double xmin;
        private double ymin;
        private double xmax;
        private double ymax;
    }
}
