package whenyourcar.gateway.whenyourcargateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import whenyourcar.gateway.whenyourcargateway.sevice.TokenService;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final TokenService tokenService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getCookies().getFirst("X-Access-Token") != null ?
                exchange.getRequest().getCookies().getFirst("X-Access-Token").getValue() : null;

        if (token != null) {
            String email = tokenService.extractAccessTokenToEmail(token);

            if (email != null && tokenService.validateAccessToken(token, email)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                return ReactiveSecurityContextHolder.getContext()
                        .map(securityContext -> {
                            securityContext.setAuthentication(authentication);
                            return securityContext;
                        })
                        .then(chain.filter(exchange));
            }
        }

        return chain.filter(exchange);
    }
}
