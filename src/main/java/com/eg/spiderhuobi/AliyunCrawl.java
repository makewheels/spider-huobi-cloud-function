package com.eg.spiderhuobi;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.github.makewheels.s3util.S3Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AliyunCrawl implements StreamRequestHandler {
    private final SpiderHandler spiderHandler = new SpiderHandler();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        InvokeUtil.init(input, output, context);
        spiderHandler.initS3();
        spiderHandler.crawl();
    }
}
