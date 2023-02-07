package com.gyr.milvusactual.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class FileUtil {

    /**
     * 将MultipartFile转换为File
     *
     *
     * @param multipartFile 参数
     * @return 执行结果
     */
    public static File multipartFileToFile(MultipartFile multipartFile) {
        File file = null;
        try {
            String fileName = multipartFile.getOriginalFilename();
            assert fileName != null;
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            file = File.createTempFile(fileName, suffix);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}
