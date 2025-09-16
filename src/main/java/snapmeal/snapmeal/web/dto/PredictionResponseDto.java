package snapmeal.snapmeal.web.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PredictionResponseDto {
    // 예측된 객체별로 생성된 DB 레코드의 ID 리스트
    private List<Long> imageId;

    private List<DetectionDto> detections;
}