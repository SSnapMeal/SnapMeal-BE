package snapmeal.snapmeal.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snapmeal.snapmeal.global.util.OpenAiConverter;
import snapmeal.snapmeal.global.OpenAiClient;
import snapmeal.snapmeal.web.dto.DietTypeRequestDto;
import snapmeal.snapmeal.web.dto.DietTypeResponseDto;


@Service
@Transactional
@RequiredArgsConstructor
public class DietTypeService {

    private final OpenAiClient openAiClient;



    public DietTypeResponseDto analyzeDietType(DietTypeRequestDto request) {
        try {
            String systemPrompt = OpenAiConverter.buildSystemPrompt();
            String userPrompt = OpenAiConverter.buildUserPrompt(request.getSelectedTypes());
            String dietType = openAiClient.requestCompletion(systemPrompt, userPrompt);
            return new DietTypeResponseDto(dietType);
        } catch (Exception e) {
            return new DietTypeResponseDto("#분석 실패");
        }
    }
}
