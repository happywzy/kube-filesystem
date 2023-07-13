package com.deri.filesystem.Controller2;

import com.deri.filesystem.entity.KubesphereLoginResponse;
import com.deri.filesystem.entity.Login;
import com.deri.filesystem.service.KubesphereService;
import com.deri.filesystem.util.RSAUtil;
import com.deri.filesystem.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: LoginController
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/31 11:03
 * @Version: v1.0
 **/
@Controller
@Slf4j
public class LoginController {

    @Autowired
    KubesphereService kubesphereService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    String login(Model model) {
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    String verify(@RequestBody Login login, HttpServletResponse response) {
        try {
            login.setPassword(RSAUtil.decryptByPrivateKey(login.getPassword()));
        } catch (Exception e) {
            log.error("RSA decrypt error", e);
            return "data error";
        }
        KubesphereLoginResponse loginResponse = kubesphereService.Login(login.getUserName(), login.getPassword());
        if (!StringUtils.isBlank(loginResponse.getAccess_token())) {
            response.addCookie(Util.ganarateCookie(login.getUserName(), false));
            return "200";
        } else {
            return "username or password error";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    String logout(HttpServletRequest request, HttpServletResponse response, Model model) {
        String token = Util.getToken(request);
        response.addCookie(Util.ganarateCookie(Util.tokens.get(token), true));
        return "redirect:/login";
    }
}
