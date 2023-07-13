package com.deri.filesystem.service;

import ch.qos.logback.core.util.FileUtil;
import com.deri.filesystem.config.FsConfig;
import com.deri.filesystem.entity.PodFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: FileService
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/26 10:19
 * @Version: v1.0
 **/
@Slf4j
@Service
public class FileService {
    public static final String CMD_LS = "ls -Alh ";
    public static final String CMD_RM = "rm -rf ";
    public static final String CMD_MK = "mkdir -p ";
    @Autowired
    FsConfig fsConfig;
    @Autowired
    KubeService kubeService;

    public List<PodFile> getFileList(String namespace, String pod, String container, String containerPath) {
        List<PodFile> list = new ArrayList<>();
        String result = kubeService.containerExec(namespace, pod, container, CMD_LS + containerPath);
        for (String line : result.split("\\n")) {
            if (line.startsWith("total") || line.startsWith("Total")) continue;
            String[] tmp = line.split("\\s+");
            if (tmp[0].startsWith("d")) {
                list.add(new PodFile(tmp[8], tmp[5] + " " + tmp[6] + " " + tmp[7], tmp[4], tmp[0], false));
            } else {
                list.add(new PodFile(tmp[8], tmp[5] + " " + tmp[6] + " " + tmp[7], tmp[4], tmp[0], true));
            }
        }
        return list;
    }

    // upload Dir, TODO
    public void uploadDir(MultipartFile[] dir) throws IOException {
        
    }

    // upload file
    public void upload(String namespace, String pod, String container, String containerPath, MultipartFile file) throws Exception {
        String localPath = getLocalPath(namespace, pod, container, containerPath);
        byte[] bytes = file.getBytes();
        Path path = Paths.get(localPath + file.getOriginalFilename());
        Files.write(path, bytes);
        kubeService.uploadFileToContainer(namespace, pod, container, localPath, containerPath, file.getOriginalFilename());
    }

    public ResponseEntity<Resource> download(String namespace, String pod, String container,
                                             String containerPath, String fileName, boolean type) throws IOException {
        String localPath = getLocalPath(namespace, pod, container, containerPath);
        File file = null;
        if (type) {
            kubeService.downloadFileFromContainer(namespace, pod, container, localPath, containerPath, fileName);
            file = new File(localPath + fileName);
        } else {
            kubeService.downloadDirectoryFromContainer(namespace, pod, container, localPath, containerPath, fileName);
            String zip = localPath + fileName + ".zip";
            zip(localPath + fileName, zip);
            file = new File(zip);
        }
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource
                (Files.readAllBytes(path));
        return ResponseEntity.ok().headers(this.headers(file.getName()))
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType
                        ("application/octet-stream")).body(resource);
    }

    public void deleteFile(String namespace, String pod, String container,
                           String containerPath, String fileName) {
        try {
            String localPath = getLocalPath(namespace, pod, container, containerPath);
            log.info("delete local {}, result {}", localPath, Files.deleteIfExists(Paths.get(localPath + fileName)));
        } catch (IOException e) {
            log.error("", e);
        }
        kubeService.containerExec(namespace, pod, container, CMD_RM + containerPath + fileName);
    }

    public void newFolder(String namespace, String pod, String container,
                          String containerPath, String fileName) {
        kubeService.containerExec(namespace, pod, container, CMD_MK + containerPath + fileName);
    }

    private String getLocalPath(String namespace, String pod, String container, String containerPath) {
        String localPath = fsConfig.getPath() + namespace + "/" + pod + "/" + container + containerPath;
        File f = new File(localPath);
        if (!f.exists()) f.mkdirs();
        return localPath;
    }

    private HttpHeaders headers(String name) {
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + name);
        header.add("Cache-Control", "no-cache, no-store,"
                + " must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return header;
    }

    private void zip(String path, String zip) {
        log.info("directory: {}", path);
        log.info("zip: {}", zip);
        try (ZipArchiveOutputStream archive = new ZipArchiveOutputStream(new FileOutputStream(zip))) {
            File folderToZip = new File(path);
            String rootPath = folderToZip.getAbsolutePath();
            Files.walk(folderToZip.toPath()).forEach(p -> {
                File file = p.toFile();
                if (!file.isDirectory()) {
                    ZipArchiveEntry entry_1 = new ZipArchiveEntry(getEntryName(file, rootPath));
                    try (FileInputStream fis = new FileInputStream(file)) {
                        archive.putArchiveEntry(entry_1);
                        IOUtils.copy(fis, archive);
                        archive.closeArchiveEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            archive.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getEntryName(File f, String rootPath) {
        String entryName;
        String fPath = f.getAbsolutePath();
        if (fPath.indexOf(rootPath) != -1)
            entryName = fPath.substring(rootPath.length() + 1);
        else
            entryName = f.getName();
        if (f.isDirectory())
            entryName += "/";
        return entryName;
    }

}
