package snapmeal.snapmeal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// RestTemplate를 스프링 Bean으로 등록하는 설정 클래스
@Configuration
public class RestTemplateConfigure {

    /**
     * RestTemplate Bean 등록
     * - @Bean: 이 메서드가 반환하는 객체를 스프링 컨테이너에 등록해줘
     * - 메서드 이름(restTemplate)이 Bean 이름이 됨
     */
    @Bean
    public RestTemplate restTemplate() {
        // 기본 설정으로 사용하는 RestTemplate 생성
        return new RestTemplate();
    }
}
