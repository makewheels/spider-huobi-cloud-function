package com.eg.spiderhuobi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.makewheels.s3util.S3Config;
import com.github.makewheels.s3util.S3Service;

import java.io.File;
import java.time.Instant;

public class SpiderHandler {
    private final S3Service s3Service = new S3Service();

    public void initS3() {
        S3Config s3Config = new S3Config();
        s3Config.setEndpoint(System.getenv("s3_endpoint"));
        s3Config.setRegion(System.getenv("s3_region"));
        s3Config.setBucketName(System.getenv("s3_bucketName"));
        s3Config.setAccessKey(System.getenv("s3_accessKey"));
        s3Config.setSecretKey(System.getenv("s3_secretKey"));
        s3Service.init(s3Config);
    }

    private JSONObject getConfig() {
        File configFile = new File(SpiderHandler.class.getResource("/config.json").getPath());
        String configJson = FileUtil.readUtf8String(configFile);
        return JSONObject.parseObject(configJson);
    }

    public void crawl() {
        JSONObject config = getConfig();
        JSONArray requests = config.getJSONArray("requests");
        for (int i = 0; i < requests.size(); i++) {
            JSONObject request = requests.getJSONObject(i);
            String requestName = request.getString("requestName");
            JSONObject source = request.getJSONObject("source");
            String requestUrl = source.getString("url");
            String response = HttpUtil.get(requestUrl);
            save(config, requestName, response, requestUrl);
        }
    }

    private void save(JSONObject config, String requestName, String response, String requestUrl) {
        JSONObject target = config.getJSONObject("target");
        String missionName = config.getString("missionName");
        String path = target.getString("path");
        path = path.replace("${missionName}", missionName);
        path = path.replace("${requestName}", requestName);

        //保存数据文件
        String dataId = IdUtil.getSnowflake().nextIdStr();
        String invokeId = InvokeUtil.getInvokeId();
        String dataFileName = invokeId + "-" + dataId + ".data";
        String basePath = path;
        path = path.replace("${fileName}", dataFileName);
        String dataFileObjectKey = path;
        s3Service.putObject(path, response);
        System.out.println("SAVE " + path);

        //保存描述信息文件
        JSONObject info = new JSONObject();
        info.put("crawlVersion", config.getString("crawlVersion"));
        info.put("provider", "aliyun-fc");
        info.put("createTime", Instant.now().toString());
        info.put("missionName", missionName);
        info.put("requestName", requestName);
        info.put("requestUrl", requestUrl);

        info.put("invokeId", invokeId);
        info.put("dataId", dataId);
        info.put("dataFileName", dataFileName);

        basePath = basePath.replace("${fileName}", dataFileName + ".info");
        String infoFileObjectKey = basePath;
        info.put("dataFileObjectKey", dataFileObjectKey);
        info.put("infoFileObjectKey", infoFileObjectKey);

        info.put("providerParams", InvokeUtil.getProviderParams());

        System.out.println("上传对象存储：" + infoFileObjectKey);
        s3Service.putObject(infoFileObjectKey, info.toJSONString());

    }

}
