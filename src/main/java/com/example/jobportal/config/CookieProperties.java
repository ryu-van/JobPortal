package com.example.jobportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "app.security.cookie")
@Getter
@Setter
public class CookieProperties {

    private boolean secure = false;

    private String sameSite = "Lax";

    private String path = "/";

    private boolean httpOnly = true;

    private String accessTokenName = "access_token";

    private String refreshTokenName = "refresh_token";

    public String getSameSiteValue() {
        return sameSite;
    }
}
