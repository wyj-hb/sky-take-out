package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file)
    {
        log.info("文件上传{}",file);
        try {
            //原始文件名
            String name =  file.getOriginalFilename();
            String substring = name.substring(name.lastIndexOf("."));
            String objectname =  UUID.randomUUID().toString() + substring;
            String file_path = aliOssUtil.upload(file.getBytes(), objectname);
            return Result.success(file_path);
        } catch (IOException e) {
            log.error("文件上传失败:{}",e);
        }
        //上传到阿里云服务器
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
