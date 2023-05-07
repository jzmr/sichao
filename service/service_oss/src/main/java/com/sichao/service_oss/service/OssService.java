package com.sichao.service_oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: OSS对象存储服务类接口
 * @author: sjc
 * @createTime: 2023年05月07日 14:38
 */
public interface OssService {
    //上传文件并返回文件在oss中的url地址
    String uploadFile(MultipartFile file);
}
