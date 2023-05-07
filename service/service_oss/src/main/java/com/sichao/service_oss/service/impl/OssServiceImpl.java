package com.sichao.service_oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.sichao.service_oss.service.OssService;
import com.sichao.service_oss.utils.ConstantAliyunOss;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @Description: OSS对象存储服务类
 * @author: sjc
 * @createTime: 2023年05月07日 14:39
 */
@Service
public class OssServiceImpl implements OssService {
    //上传文件并返回文件在oss中的url地址
    @Override
    public String uploadFile(MultipartFile file) {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com
        String endpoint = ConstantAliyunOss.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantAliyunOss.ACCESS_KEY_ID;
        String accessKeySecret = ConstantAliyunOss.ACCESS_KEY_SECRET;
        String bucketName=ConstantAliyunOss.BUCKET_NAME;

        try{
            //创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            //填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
            InputStream inputStream=file.getInputStream();
            //获取文件名称
            String filename = file.getOriginalFilename();

            //1、在文件名称里面加上随机唯一的值
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            filename=uuid+filename;
            //2、把文件按照日期进行分类，获取当前日期
            String datePath = new DateTime().toString("yyyy/MM/dd");
            //3、拼接
            filename=datePath+"/"+filename;//这样在Oss的Bucket仓库汇总就会自动按照日期进行分类

            //填写Bucket名称和Object完整路径.Object完整路劲中不能包含Bucket名称
            //参数1：Bucket仓库名，参数2：上传到Oss的文件路径和文件名称，参数3：上传文件输入流
            ossClient.putObject(bucketName,filename,inputStream);
            //关闭OSSClient
            ossClient.shutdown();

            //把上传之后的文件路径返回，需要把上传到阿里云oss路径手动拼接出来
            String url="https://"+bucketName+"."+endpoint+"/"+filename;
            return url;
        }catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
