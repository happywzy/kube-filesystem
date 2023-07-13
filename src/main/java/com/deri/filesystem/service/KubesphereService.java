package com.deri.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.deri.filesystem.config.FsConfig;
import com.deri.filesystem.entity.KubesphereLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName: kubesphereService
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/31 9:46
 * @Version: v1.0
 **/
@Service
public class KubesphereService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    FsConfig fsConfig;

    public KubesphereLoginResponse Login(String userName, String password) {
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("grant_type", "password");
        postParameters.add("username", userName);
        postParameters.add("password", password);
        postParameters.add("client_id", "kubesphere");
        postParameters.add("client_secret", "kubesphere");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> r = new HttpEntity<>(postParameters, headers);
        String url = fsConfig.getKsApi() + "/oauth/token";
        KubesphereLoginResponse response = null;
        try {
            response = restTemplate.postForObject(url, r, KubesphereLoginResponse.class);
        } catch (HttpClientErrorException e) {
            response = JSON.parseObject(e.getResponseBodyAsString(), KubesphereLoginResponse.class);
        }
        return response;
    }
}
