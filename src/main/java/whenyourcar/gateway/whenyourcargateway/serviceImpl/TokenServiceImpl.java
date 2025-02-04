package whenyourcar.gateway.whenyourcargateway.serviceImpl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import whenyourcar.gateway.whenyourcargateway.sevice.TokenService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.access.secret}")
    private String JWT_ACCESS_SECRET;

    @Value("${jwt.refresh.secret}")
    private String JWT_REFRESH_SECRET;

    @Value("${jwt.access.expire}")
    private Long JWT_ACCESS_EXPIRE;

    @Value("${jwt.refresh.expire}")
    private Long JWT_REFRESH_EXPIRE;

    @Value("${jwt.refresh.prefix}")
    private String REFRESH_TOKEN_PREFIX;

    @Override
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_ACCESS_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, JWT_ACCESS_SECRET)
                .compact();
    }

    @Override
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_REFRESH_EXPIRE)) // 7 days expiration for Refresh Token
                .signWith(SignatureAlgorithm.HS256, JWT_REFRESH_SECRET)
                .compact();
    }

    @Override
    public boolean validateAccessToken(String token, String email) {
        String user = extractAccessTokenToEmail(token);
        return (user.equals(email) && !isAccessTokenExpired(token));
    }

    @Override
    public boolean validateRefreshToken(String token, String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        Object storedToken = redisTemplate.opsForHash().get(key, "refreshToken");

        return storedToken != null && storedToken.equals(token);
    }


    @Override
    public String extractAccessTokenToEmail(String acessToken) {
        return Jwts.parser()
                .setSigningKey(JWT_ACCESS_SECRET)
                .parseClaimsJws(acessToken)
                .getBody()
                .getSubject();
    }

    @Override
    public String extractRefreshTokenToEmail(String refreshToken) {
        return Jwts.parser()
                .setSigningKey(JWT_REFRESH_SECRET)
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject();
    }

    @Override
    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("refreshToken", refreshToken);
        tokenData.put("expiry", JWT_REFRESH_EXPIRE);

        redisTemplate.opsForHash().putAll(key, tokenData);
        redisTemplate.expire(key, JWT_REFRESH_EXPIRE, TimeUnit.MILLISECONDS);
    }


    private boolean isAccessTokenExpired(String accessToken) {
        return extractAccessTokenExpiration(accessToken).before(new Date());
    }

    // Extract expiration date from JWT token
    private Date extractAccessTokenExpiration(String accessToken) {
        return Jwts.parser()
                .setSigningKey(JWT_ACCESS_SECRET)
                .parseClaimsJws(accessToken)
                .getBody()
                .getExpiration();
    }
}
