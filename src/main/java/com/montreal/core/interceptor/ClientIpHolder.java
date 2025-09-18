package com.montreal.core.interceptor;

public class ClientIpHolder {

    private static final ThreadLocal<String> clientIpThreadLocal = new ThreadLocal<>();

    public static void setClientIp(String clientIp) {
        clientIpThreadLocal.set(clientIp);
    }

    public static String getClientIp() {
        return clientIpThreadLocal.get();
    }

    public static void clear() {
        clientIpThreadLocal.remove();
    }

}