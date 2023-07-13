package com.deri.filesystem.aop;

import com.deri.filesystem.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.deri.filesystem.util.Util.getToken;


/**
 * @ClassName: LoginAop
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2019/12/6 15:28
 * @Version: v1.0
 **/
@Aspect
@Component
@Slf4j
public class LoginAop {
    public static final String TOKEN = "kfs_token";

    @Pointcut("execution(public * com.deri.filesystem.controller.*.*(..))")
    public void verify() {
    }

    @Before("verify()")
    public void doVerify() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = getToken(attributes.getRequest());
        if (token == null) {
            throw new AuthorizeException();
        }
        if (!Util.tokens.containsKey(token)) {
            throw new AuthorizeException();
        }
    }
}
