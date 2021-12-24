package com.eg.spiderhuobi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionParam;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.makewheels.s3util.S3Config;
import com.github.makewheels.s3util.S3Service;
import lombok.val;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
            int finalI = i;
            JSONObject request = requests.getJSONObject(finalI);
            String requestName = request.getString("requestName");
            JSONObject source = request.getJSONObject("source");
            String requestUrl = source.getString("url");
            String response = HttpUtil.get(requestUrl);
            save(config, requestName, response, requestUrl);
        }
    }

    private JSONObject getProviderParams() {
        JSONObject providerParams = new JSONObject();
        Context context = InvokeUtil.getContext();
        String requestId = context.getRequestId();
        FunctionParam functionParam = context.getFunctionParam();
        providerParams.put("requestId", requestId);
        providerParams.put("functionParam", functionParam);
        return providerParams;
    }

    private void save(JSONObject config, String requestName, String response, String requestUrl) {
        JSONObject target = config.getJSONObject("target");
        String missionName = config.getString("missionName");
        String path = target.getString("path");
        path = path.replace("${missionName}", missionName);
        path = path.replace("${requestName}", requestName);

        //保存数据文件
        String dataId = IdUtil.getSnowflake().nextIdStr();
        String fileBaseName = InvokeUtil.getInvokeId() + "-" + dataId;
        String basePath = path;
        path = path.replace("${fileName}", fileBaseName + ".data");
        s3Service.putObject(path, response);
        System.out.println("SAVE " + path);

        //保存描述信息文件
        JSONObject info = new JSONObject();
        info.put("version", "1");
        info.put("provider", "aliyun-fc");
        info.put("createTime", Instant.now().toString());
        info.put("invokeId", InvokeUtil.getInvokeId());
        info.put("dataId", dataId);
        info.put("missionName", missionName);
        info.put("requestName", requestName);
        info.put("requestUrl", requestUrl);

        info.put("providerParams", getProviderParams());

        basePath = basePath.replace("${fileName}", fileBaseName + ".data.info");
        s3Service.putObject(basePath, info.toJSONString());

        System.out.println("SAVE " + basePath);

    }

}
