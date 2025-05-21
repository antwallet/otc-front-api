package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.vo.TComplaintVo;
import cn.com.otc.modular.sys.service.TComplaintService;
import cn.hutool.core.date.DateTime;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.UUID;

/**
 * @description: 申述控制类
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Slf4j
@RestController
@RequestMapping("/api/front/complaint")
public class TComplaintController {

    @Autowired
    private TComplaintService tComplaintService;

    @Autowired
    private CheckTokenUtil checkTokenUtil;

    @Resource
    private MyCommonConfig myCommonConfig;

    @PostMapping("/uploadFile")
    ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
        String uploadUrl = null;
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = myCommonConfig.getOssEndpoint();
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        String accessKeyId = myCommonConfig.getOssAccessKeyId();
        String accessKeySecret = myCommonConfig.getOssAccessKeySecret();
        //EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "packet-gift";
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        //构建日期层级文件夹:2020/12/07
        String filePath = new DateTime().toString("yyyy/MM/dd");
        //String objectName = "exampledir/exampleobject.txt";
        String original = file.getOriginalFilename();
        String fileType = original.substring(original.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString();
        String newName = fileName + fileType;
        //上传文件在OSS中存储位置:2020/12/07/uuid随机数.扩展名
        String fileUrl = filePath + "/" + newName;
        // 创建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 获取上传文件输入流
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, fileUrl, inputStream);
            ossClient.setObjectAcl(bucketName, fileUrl, CannedAccessControlList.PublicRead);
            uploadUrl = "https://" + bucketName + "." + endpoint + "/" + fileUrl;
        } catch (Exception e) {
            log.error("上传文件异常：{}", e.getMessage(), e);
            return ResponseEntity.failure("上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ResponseEntity.success("上传成功", uploadUrl);
    }


    //@PostMapping("/createComplaintRecord")
    public R createComplaintRecord(HttpServletRequest httpRequest,
                                   @RequestBody TComplaintVo tComplaintVo) {
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isBlank(lang)) {
            lang = "en-US";
        }
        try {
            /**
             * 1、根据token获取用户信息
             */
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            tComplaintService.createComplaintRecord(tComplaintVo,lang,userInfoResult);
            return R.ok();
        } catch (Exception e) {
            log.error("更新是否展示弹窗失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }




}
