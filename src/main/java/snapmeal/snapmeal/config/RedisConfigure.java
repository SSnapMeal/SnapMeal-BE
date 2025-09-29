package snapmeal.snapmeal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import snapmeal.snapmeal.web.dto.TodayNutritionResponseDto;
import snapmeal.snapmeal.web.dto.TodayRecommendationResponseDto;

@Configuration
public class RedisConfigure {
    // yml, properties 파일에서 host, port 정보 가져옴
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
    @Bean
    public RedisTemplate<String, TodayRecommendationResponseDto> recommendationRedisTemplate(ObjectMapper objectMapper) {
        RedisTemplate<String, TodayRecommendationResponseDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer<TodayRecommendationResponseDto> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, TodayRecommendationResponseDto.class);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }

    // TodayNutrition 전용 RedisTemplate
    @Bean
    public RedisTemplate<String, TodayNutritionResponseDto> nutritionRedisTemplate(ObjectMapper objectMapper) {
        RedisTemplate<String, TodayNutritionResponseDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<TodayNutritionResponseDto> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, TodayNutritionResponseDto.class);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }

}
