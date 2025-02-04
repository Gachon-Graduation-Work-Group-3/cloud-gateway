package whenyourcar.gateway.whenyourcargateway.sevice;

import java.util.Date;

public interface TokenService {
    public String generateAccessToken(String email);
    public String generateRefreshToken(String email);
    public boolean validateAccessToken(String token, String email);
    public boolean validateRefreshToken(String token, String email);
    public String extractAccessTokenToEmail(String acessToken);
    public String extractRefreshTokenToEmail(String refreshToken);
    public void saveRefreshToken(String email, String refreshToken);
}
