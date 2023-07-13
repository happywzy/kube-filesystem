package com.deri.filesystem.util;

import com.deri.filesystem.aop.LoginAop;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: Util
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/31 11:24
 * @Version: v1.0
 **/
public class Util {
    public static Map<String/*token*/, String/*username*/> tokens = new HashMap<>();

    public static String getToken() {
        return UUID.randomUUID().toString();
    }

    public static Cookie ganarateCookie(String userName, boolean delete) {
        String token = getToken();
        tokens.put(token, userName);
        Cookie cookie = new Cookie(LoginAop.TOKEN, token);
        if (delete) cookie.setMaxAge(0);
        else cookie.setMaxAge(99999999);
        cookie.setPath("/");
        return cookie;
    }

    public static String getUsername(HttpServletRequest request) {
        String token = getToken(request);
        return tokens.get(token);
    }

    public static String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(LoginAop.TOKEN)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
