package snapmeal.snapmeal.repository;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RefreshTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public String getRefreshToken(Long userId) {
        String key = "refreshToken:" + userId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteTokenByUserId(Long userId) {
        String key = "refreshToken:" + userId;
        redisTemplate.delete(key);
    }

    public boolean existsByUserId(Long userId) {
        String key = "refreshToken:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void saveToken(Long userId, String refreshToken, long expirationTime) {
        String key = "refreshToken:" + refreshToken; //
        redisTemplate.opsForValue().set(key, userId.toString(), expirationTime / 1000, TimeUnit.SECONDS);
    }

    // Refresh Token으로 userId 가져오기
    public Long getUserIdByToken(String refreshToken) {
        String key = "refreshToken:" + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(key);
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    // Refresh Token 삭제
    public void deleteToken(String refreshToken) {
        String key = "refreshToken:" + refreshToken;
        redisTemplate.delete(key);
    }

    public boolean existsByToken(String refreshToken) {
        String key = "refreshToken:" + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    public void deleteAllTokensByUserId(Long userId) {
        String pattern = "refreshToken:*";
        String target = String.valueOf(userId);

        var keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            String storedUserId = redisTemplate.opsForValue().get(key);
            if (target.equals(storedUserId)) {
                redisTemplate.delete(key);
            }
        }
    }
}
