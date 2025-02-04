package whenyourcar.gateway.whenyourcargateway.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_CLIENT", "사용자"),
    DEALER("ROLE_DEALER", "딜러");

    private final String key;
    private final String title;
}
