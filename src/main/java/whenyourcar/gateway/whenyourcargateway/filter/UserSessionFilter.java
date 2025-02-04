package whenyourcar.gateway.whenyourcargateway.filter;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import whenyourcar.gateway.whenyourcargateway.sevice.TokenService;

@Component
@RequiredArgsConstructor
public class UserSessionFilter implements GlobalFilter, Ordered {
    private final TokenService tokenService;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getCookies().getFirst("X-Access-Token") != null ?
                exchange.getRequest().getCookies().getFirst("X-Access-Token").getValue() : null;

        if (token != null) {
            String email = tokenService.extractAccessTokenToEmail(token);

            if (email != null) {
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", email)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
        }
        else {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().set("Content-Type", "application/json");

            String errorMessage = "{\"error\": \"Unauthorized\", \"message\": \"Token is missing or invalid\"}";
            DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes());
            return response.writeWith(Mono.just(buffer));
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
