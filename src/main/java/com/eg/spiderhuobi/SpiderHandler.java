package com.eg.spiderhuobi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.makewheels.s3util.S3Config;
import com.github.makewheels.s3util.S3Service;

import java.io.File;

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
            String url = source.getString("url");
            String response = HttpUtil.get(url);
            save(config, requestName, response);
        }
    }

    private void save(JSONObject config, String requestName, String response) {
        JSONObject target = config.getJSONObject("target");
        String missionName = config.getString("missionName");
        String path = target.getString("path");
        path = path.replace("${missionName}", missionName);
        path = path.replace("${requestName}", requestName);
        path = path.replace("${currentTime}", System.currentTimeMillis() + "");
        s3Service.putObject(path, response);
        System.out.println("SAVE " + path);
    }

}
