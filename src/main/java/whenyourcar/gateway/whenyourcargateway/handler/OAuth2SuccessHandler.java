package whenyourcar.gateway.whenyourcargateway.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import whenyourcar.gateway.whenyourcargateway.data.entity.User;
import whenyourcar.gateway.whenyourcargateway.data.enums.Role;
import whenyourcar.gateway.whenyourcargateway.repository.UserRepository;
import whenyourcar.gateway.whenyourcargateway.sevice.TokenService;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if (!userRepository.existsUserByEmail(oAuth2User.getAttribute("email"))) {
            userRepository.save(User.builder()
                    .email(oAuth2User.getAttribute("email"))
                    .name(oAuth2User.getAttribute("name"))
                    .picture(oAuth2User.getAttribute("picture"))
                    .role(Role.USER)
                    .build());
        }
        String email = oAuth2User.getAttribute("email");
        String targetUrl = UriComponentsBuilder.newInstance()
                .scheme("http") // HTTPS라면 "https"로 변경
                .host("localhost") // 배포 환경에서는 클라이언트 도메인으로 변경
                .port(3000) // React의 포트
                .path("/login-success") // React 페이지의 경로
                .build()
                .toUriString();

        String accessToken = tokenService.generateAccessToken(email);
        String refreshToken = tokenService.generateRefreshToken(email);

        tokenService.saveRefreshToken(email, refreshToken);

        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(java.net.URI.create(targetUrl));
        response.getHeaders().add(HttpHeaders.SET_COOKIE, "X-Access-Token=" + accessToken + "; Path=/; HttpOnly; SameSite=None; Secure; Max-Age=1800");
        response.getHeaders().add(HttpHeaders.SET_COOKIE, "X-Refresh-Token=" + refreshToken + "; Path=/; HttpOnly; SameSite=None; Secure; Max-Age=604800");


        return response.setComplete();
    }
}
