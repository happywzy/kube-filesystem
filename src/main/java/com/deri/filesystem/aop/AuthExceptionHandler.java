package com.deri.filesystem.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName: AuthExceptionHandler
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2019/12/6 15:43
 * @Version: v1.0
 **/
@ControllerAdvice
@Slf4j
public class AuthExceptionHandler {

    @ExceptionHandler(value =  AuthorizeException.class)
    public ModelAndView handlerAuthorizeException(){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/login");
        return mav;
    }
}
