package com.deri.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: PodFile
 * @Description: TODO
 * @Author: wuzhiyong
 * @Time: 2022/8/26 10:43
 * @Version: v1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodFile {
    // file name
    private String name;
    // file time
    private String time = "-";
    // file size
    private String size = "-";
    // right
    private String right = "-";
    /**
     * file type: file or dir
     * file : true
     * dir  : false
     */
    private boolean type = true;

    public PodFile(String name) {
        this.name = name;
    }

    public PodFile(String name, boolean type) {
        this.name = name;
        this.type = type;
    }
}
