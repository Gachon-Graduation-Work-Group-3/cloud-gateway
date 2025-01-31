package whenyourcar.gateway.whenyourcargateway.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class OAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {
    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        webFilterExchange.getExchange().getRequest().getAttributes().put("userName", oAuth2User.getName());
        webFilterExchange.getExchange().getRequest().getAttributes().put("email", oAuth2User.getAttribute("email"));


        String targetUrl = UriComponentsBuilder.newInstance()
                .scheme("http") // HTTPS라면 "https"로 변경
                .host("localhost") // 배포 환경에서는 클라이언트 도메인으로 변경
                .port(3000) // React의 포트
                .path("/login-success") // React 페이지의 경로
                .build()
                .toUriString();

        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND); // 302 Redirect
        response.getHeaders().setLocation(java.net.URI.create(targetUrl));

        return response.setComplete();
    }
}
