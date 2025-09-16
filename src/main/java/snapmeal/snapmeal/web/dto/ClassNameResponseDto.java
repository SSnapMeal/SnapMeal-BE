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
public class ClassNameResponseDto {
    @Schema(description = "예측된 클래스 이름", example = "간장게장")
    @JsonProperty("class_name")
    private String className;
}
