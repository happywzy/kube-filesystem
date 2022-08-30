package com.deri.filesystem.controller;

import com.deri.filesystem.service.KubeService;
import io.fabric8.kubernetes.api.model.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName: TestController
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/24 10:15
 * @Version: v1.0
 **/
@RestController
public class TestController {

    @Autowired
    KubeService kubeService;

    @GetMapping("/test")
    public void test(){
//        kubeService.containerList();
    }
}
