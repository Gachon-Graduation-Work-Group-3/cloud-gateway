package whenyourcar.gateway.whenyourcargateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import whenyourcar.gateway.whenyourcargateway.sevice.TokenService;

import java.net.URI;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final TokenService tokenService;
    @GetMapping("/renew")
    public Mono<ResponseEntity<String>> renewAccessToken(@CookieValue("X-Refresh-Token") String refreshToken, ServerWebExchange exchange) {
        String email = tokenService.extractRefreshTokenToEmail(refreshToken);
        if (tokenService.validateRefreshToken(refreshToken, email)) {
            String newAccessToken = tokenService.generateAccessToken(email);

            ResponseCookie cookie = ResponseCookie.from("X-Access-Token", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(Duration.ofMinutes(30))
                    .build();

            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body("Access token renewed successfully"));
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
            exchange.getResponse().getHeaders().setLocation(URI.create("http://localhost:3000/social-login"));

            return Mono.empty();
        }
    }
}
