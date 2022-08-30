package com.deri.filesystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName: Config
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/25 10:28
 * @Version: v1.0
 **/
@Data
@ConfigurationProperties(prefix = "kube.fs")
@Component
public class FsConfig {
    private String token;
    private String masterUrl;
    private String path;
}
