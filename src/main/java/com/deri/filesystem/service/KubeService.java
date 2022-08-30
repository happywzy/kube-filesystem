package com.deri.filesystem.service;

import com.deri.filesystem.config.FsConfig;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName: KubeService
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/24 10:09
 * @Version: v1.0
 * @SourceUrl: https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-examples/src/main/java/io/fabric8/kubernetes/examples/kubectl/equivalents
 **/
@Service
@Slf4j
public class KubeService {

    private KubernetesClient client;
    @Autowired
    FsConfig fsConfig;

    @PostConstruct
    private void kubeInit() {
        Config config = new ConfigBuilder()
                .withMasterUrl(fsConfig.getMasterUrl())
                .withOauthToken(fsConfig.getToken())
                .withTrustCerts(true)
                .build();
        client = new DefaultKubernetesClient(config);
    }

    public List<String> getNamespaces() {
        return client.namespaces().list().getItems().stream().map(s -> s.getMetadata().getName()).collect(Collectors.toList());
    }

    public List<String> getPods(String namespace) {
        List<String> pods = client.pods().inNamespace(namespace).list()
                .getItems()
                .stream()
                .map(Pod::getMetadata)
                .map(ObjectMeta::getName)
                .collect(Collectors.toList());
        return pods;
    }

    public List<String> getContainers(String namespace, String pod) {
        List<String> containers =
                client.pods().inNamespace(namespace)
                        .withName(pod).get().getSpec()
                        .getContainers().stream().map(Container::getName).collect(Collectors.toList());
        return containers;
    }

    public void uploadFileToContainer(String namespace, String pod, String container,
                                      String localPath, String containerPath, String fileName) {
        File fileToUpload = new File(localPath + fileName);
        try {
            client.pods().inNamespace(namespace)
                    .withName(pod)
                    .inContainer(container)
                    .file(containerPath + fileName)
                    .upload(fileToUpload.toPath());
            log.info("upload file {} to container success, namespace:{}, pod:{}, container:{}", fileName, namespace, pod, container);
        } catch (Exception e) {
            throw e;
        }
    }

    public void uploadDirectoryToPod(String namespace, String pod, String container) {
        File fileToUpload = new File(fsConfig.getPath());
        client.pods().inNamespace("test")
                .withName("test-v1-78bbccf595-bpjst")
                .dir("/tmp/test/dir")
                .upload(fileToUpload.toPath());

    }

    public void downloadFileFromContainer(String namespace, String pod, String container,
                                          String localPath, String containerPath, String fileName) {
        File f = new File(localPath);
        if (!f.exists()) f.mkdirs();
        Path downloadToPath = new File(localPath + fileName).toPath();
        client.pods()
                .inNamespace(namespace)
                .withName(pod)
                .inContainer(container)
                .file(containerPath + fileName)
                .copy(downloadToPath);
    }

    public void downloadDirectoryFromContainer(String namespace, String pod, String container,
                                               String localPath, String containerPath, String dir) {
        File f = new File(localPath + dir);
        if (!f.exists()) f.mkdirs();
        String result = containerExec(namespace, pod, container, FileService.CMD_LS + containerPath + dir);
        for (String line : result.split("\\n")) {
            if (line.startsWith("total") || line.startsWith("Total")) continue;
            String[] tmp = line.split("\\s+");
            String fileName = tmp[8];
            if (tmp[0].startsWith("d")) {
                downloadDirectoryFromContainer(namespace, pod, container, localPath + dir + "/", containerPath + dir + "/", fileName);
            } else {
                downloadFileFromContainer(namespace, pod, container, localPath + dir + "/", containerPath + dir + "/", fileName);
            }
        }
    }

    public String containerExec(String namespace, String pod, String container, String cmd) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            CountDownLatch execLatch = new CountDownLatch(1);
            ExecWatch execWatch = client.pods().inNamespace(namespace)
                    .withName(pod).inContainer(container)
                    .writingOutput(out)
                    .writingError(out)
//                    .withTTY()
                    .usingListener(new MyPodExecListener(namespace, pod, container, execLatch, cmd))
                    .exec(cmd.split(" "));
            boolean latchTerminationStatus = execLatch.await(10, TimeUnit.SECONDS);
            if (!latchTerminationStatus) {
                log.warn("Latch could not terminate within specified time");
            }
            execWatch.close();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for the exec: {}", e.getMessage());
        }
        return out.toString();
    }

    private class MyPodExecListener implements ExecListener {
        private CountDownLatch execLatch;
        private String namespace;
        private String pod;
        private String container;
        private String cmd;

        public MyPodExecListener(String namespace, String pod, String container, CountDownLatch execLatch, String cmd) {
            this.execLatch = execLatch;
            this.namespace = namespace;
            this.pod = pod;
            this.container = container;
            this.cmd = cmd;
        }

        @Override
        public void onOpen() {
            log.info("namespace:{}, pod:{}, container:{}, cmd:{},Shell was opened", namespace, pod, container, cmd);
        }

        @Override
        public void onFailure(Throwable t, Response failureResponse) {
            log.info("namespace:{}, pod:{}, container:{}, cmd:{},Shell was failure, error: {}", namespace, pod, container, cmd, t.getMessage());
            execLatch.countDown();
        }

        @Override
        public void onClose(int code, String reason) {
            log.info("namespace:{}, pod:{}, container:{}, cmd:{},Shell was close, code: {}, reason: {}", namespace, pod, container, cmd, code, reason);
            execLatch.countDown();
        }
    }
}
