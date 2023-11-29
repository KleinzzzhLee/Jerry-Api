package com.api.project.service.commonservice;

import cn.hutool.core.util.RandomUtil;
import com.api.project.common.ErrorCode;
import com.api.project.exception.BusinessException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileService {

    private final String pathUserSource = "D:/IDEA Projects/mineProjects/API-project/Jerry-Api/JerryProperties";

    /**
     * 创建头像
     * @param userId 用户的id
     * @param img 图片
     * @return
     * @throws IOException
     */
    public String createImgFile(Long userId, MultipartFile img) throws IOException {

        // 随机生成文件名
        String originName = RandomUtil.randomString(18);
        // 获得文件的类型
        String originType = img.getContentType().split("/")[1];
        String relativePath = "/information/users/" + userId + "/sculptureImg/";
        String fileName = originName + "." + originType;


        // 在resources目录下创建目录
//        File dictionary = new File("classpath:"  + relativePath);
        File dictionary = new File(pathUserSource + relativePath);
        if(!dictionary.exists()) {
            if (!dictionary.mkdirs()) {
                throw new BusinessException(ErrorCode.FILE_CREATE_ERROR);
            }
        } else {
            // 将文件夹的内容清空
            FileUtils.cleanDirectory(dictionary);
        }

        // 创建图片文件
        File file = new File(dictionary, fileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new BusinessException(ErrorCode.FILE_CREATE_ERROR);
            }
        }

        FileCopyUtils.copy(img.getBytes(), file);

        return relativePath + fileName;
    }
}
