package snapmeal.snapmeal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponseDto {

    @Schema(description = "예측된 클래스 ID", example = "2")
    @JsonProperty("class_id")
    private int classId;
}
