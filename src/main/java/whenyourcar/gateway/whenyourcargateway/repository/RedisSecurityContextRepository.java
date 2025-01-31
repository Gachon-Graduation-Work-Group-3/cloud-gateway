package whenyourcar.gateway.whenyourcargateway.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisSecurityContextRepository implements ServerSecurityContextRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return exchange.getSession()
                .flatMap(session -> {
                    if (session != null) {
                        String redisKey = getRedisKey(session);
                        Map<String, Object> securityContextMap = convertSecurityContextToMap(context);
                        return Mono.fromRunnable(() -> redisTemplate.opsForHash().putAll(redisKey, securityContextMap));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    if (session != null) {
                        String redisKey = getRedisKey(session);
                        return Mono.justOrEmpty(redisTemplate.opsForHash().entries(redisKey))
                                .map(this::convertMapToSecurityContext);
                    }
                    return Mono.empty();
                });
    }

    // Redis 키 생성 메서드
    private String getRedisKey(WebSession session) {
        return "securityContext:" + session.getId();
    }

    private Map<String, Object> convertSecurityContextToMap(SecurityContext context) {
        Map<String, Object> map = new HashMap<>();
        if (context.getAuthentication() != null) {
            map.put("principal", context.getAuthentication().getPrincipal().toString());
            map.put("authorities", context.getAuthentication().getAuthorities().toString());
            map.put("credentials", context.getAuthentication().getCredentials().toString());
        }
        return map;
    }

    private SecurityContext convertMapToSecurityContext(Map<Object, Object> map) {
        SecurityContextImpl securityContext = new SecurityContextImpl();
        String principal = (String) map.get("principal");
        String authorities = (String) map.get("authorities");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.commaSeparatedStringToAuthorityList(authorities)
        );
        securityContext.setAuthentication(authentication);

        return securityContext;
    }
}
