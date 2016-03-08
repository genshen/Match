package com.holo.web.tools.data_set;

import java.io.File;

/**
 * Created by 根深 on 2016/3/6.
 */
public class MediaInfo {
    public File file = null;
    public String mime = "";

    public MediaInfo() {}

    public MediaInfo(String path,String mime) {
        file = new File(path);
        this.mime  = mime;
    }

    public boolean ilLegal(){
        return (file == null || !file.exists() || mime.isEmpty());
    }
}
