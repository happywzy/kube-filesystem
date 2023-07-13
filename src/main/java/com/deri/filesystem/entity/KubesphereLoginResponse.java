package com.deri.filesystem.entity;

import lombok.Data;
import lombok.ToString;

/**
 * @ClassName: KubesphereLoginResponse
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/31 9:55
 * @Version: v1.0
 **/
@Data
@ToString
public class KubesphereLoginResponse {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String error;
    private String error_description;
}
