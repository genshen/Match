package com.holo.m.tools.files;

import com.holo.m.tools.Tools;

import java.io.File;
import java.io.Serializable;

/**
 * Created by 根深 on 2016/1/27.
 */
public class BasicFileInformation implements Serializable {
    public long size = 0;
    public String path, name;

    public BasicFileInformation(String path) {
        File file = new File(path);
        this.path = path;
        this.name = file.getName();
        this.size = file.length();
    }

    public byte[] getHeaderBytes() {
//	        check 2, long 8,int 4 ,short 2
        byte[] n = name.getBytes();
        byte[] s = Tools.toByteArray(size);
        short len = (short) (n.length + 12); // the len is file_name'length add file_size'length
        byte[] b = new byte[len];

        len -= 4; // delete the first 4 byte.
        b[0] = 'F';
        b[1] = 'I';
        b[2] = (byte) (len / 256);
        b[3] = (byte) (len % 256);
        //b[0]<<8 + b[1];

        System.arraycopy(s, 0, b, 4, 8);// src, src_pos, des, des_pos, length
        System.arraycopy(n, 0, b, 12, n.length);
        return b;
    }
}
