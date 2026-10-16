package com.gestionmicroempresas.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import org.springframework.stereotype.Component;

@Component
public class CookieBearerTokenResolver
        implements BearerTokenResolver {

    private final String cookieName;


    public CookieBearerTokenResolver(
            @Value("${app.auth.cookie-name}")
            String cookieName
    ) {
        this.cookieName =
                cookieName;
    }


    @Override
    public String resolve(
            HttpServletRequest request
    ) {

        Cookie[] cookies =
                request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (
                    cookieName.equals(
                            cookie.getName()
                    )
            ) {
                return cookie.getValue();
            }
        }

        return null;
    }
}