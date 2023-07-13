package com.deri.filesystem.controller;

import com.deri.filesystem.entity.PodFile;
import com.deri.filesystem.service.FileService;
import com.deri.filesystem.service.KubeService;
import com.deri.filesystem.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/***
 * @Author: wuzhiyong
 * @Description:
 * @Date: 2022/8/26 10:19
 * @Param:
 * @return:
 **/
@Controller
public class FileController {
    @Autowired
    FileService fileService;
    @Autowired
    KubeService kubeService;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        model.addAttribute("username", Util.getUsername(request));
        model.addAttribute("namespaces", kubeService.getNamespaces());
        return "index";
    }

    @GetMapping("/list/{namespace}/{pod}/{container}")
    @ResponseBody
    public List<PodFile> getPodFile(@PathVariable(name = "namespace") String namespace,
                                    @PathVariable(name = "pod") String pod,
                                    @PathVariable(name = "container") String container,
                                    @RequestParam(name = "path", required = false, defaultValue = "/") String path,
                                    HttpServletRequest request) {
        return fileService.getFileList(Util.getUsername(request), namespace, pod, container, path);
    }

    @GetMapping("/pod")
    @ResponseBody
    public List<String> getPods(@RequestParam String namespace) {
        return kubeService.getPods(namespace);
    }

    @GetMapping("/container")
    @ResponseBody
    public List<String> getContainers(@RequestParam(name = "namespace") String namespace,
                                      @RequestParam(name = "pod") String pod) {
        return kubeService.getContainers(namespace, pod);
    }


    @PostMapping("/upload/{namespace}/{pod}/{container}")
    @ResponseBody
    public String singleFileUpload(@PathVariable(name = "namespace") String namespace,
                                   @PathVariable(name = "pod") String pod,
                                   @PathVariable(name = "container") String container,
                                   @RequestParam(name = "path") String path,
                                   @RequestParam("file") MultipartFile file,
                                   HttpServletRequest request) {
        try {
            fileService.upload(Util.getUsername(request), namespace, pod, container, path, file);
        } catch (Exception e) {
            e.printStackTrace();
            return file.getOriginalFilename() + " upload failed! " + e.getMessage();
        }
        return file.getOriginalFilename() + " upload success!";
    }

    @GetMapping(path = "/download/{namespace}/{pod}/{container}/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable(name = "namespace") String namespace,
                                             @PathVariable(name = "pod") String pod,
                                             @PathVariable(name = "container") String container,
                                             @PathVariable("fileName") String fileName,
                                             @RequestParam(name = "path") String path,
                                             @RequestParam(name = "type") boolean type,
                                             HttpServletRequest request) throws IOException {
        return fileService.download(Util.getUsername(request), namespace, pod, container, path, fileName, type);
    }

    @PostMapping(path = "/delete/{namespace}/{pod}/{container}/{fileName}")
    @ResponseBody
    public void delete(@PathVariable(name = "namespace") String namespace,
                       @PathVariable(name = "pod") String pod,
                       @PathVariable(name = "container") String container,
                       @PathVariable("fileName") String fileName,
                       @RequestParam(name = "path") String path,
                       HttpServletRequest request) {
        fileService.deleteFile(Util.getUsername(request), namespace, pod, container, path, fileName);
    }

    @PostMapping(path = "/newfolder/{namespace}/{pod}/{container}/{folderName}")
    @ResponseBody
    public void newFolder(@PathVariable(name = "namespace") String namespace,
                          @PathVariable(name = "pod") String pod,
                          @PathVariable(name = "container") String container,
                          @PathVariable("folderName") String folderName,
                          @RequestParam(name = "path") String path,
                          HttpServletRequest request) {
        fileService.newFolder(Util.getUsername(request), namespace, pod, container, path, folderName);
    }

}
