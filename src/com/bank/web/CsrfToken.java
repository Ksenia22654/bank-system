package com.bank.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Токен защиты форм от подделки межсайтовых запросов. */
public final class CsrfToken {
    public static final String PARAMETER = "csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfToken() {
    }

    public static String forSession(HttpSession session) {
        synchronized (session.getId().intern()) {
            String token = (String) session.getAttribute(PARAMETER);
            if (token == null) {
                byte[] bytes = new byte[24];
                RANDOM.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute(PARAMETER, token);
            }
            return token;
        }
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(PARAMETER);
        String actual = request.getParameter(PARAMETER);
        return expected != null && actual != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
