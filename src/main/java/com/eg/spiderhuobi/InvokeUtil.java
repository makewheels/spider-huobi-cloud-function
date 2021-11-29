package com.eg.spiderhuobi;

import cn.hutool.core.util.IdUtil;

public class InvokeUtil {
    private static final String invokeId = IdUtil.simpleUUID();

    public static String getInvokeId() {
        return invokeId;
    }
}
