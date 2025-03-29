package com.openquartz.mqdegrade.sender.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

@Slf4j
public class IpUtils {

    private IpUtils() {
    }

    private static String localIp;

    public static String getIp() {
        if (localIp != null) {
            return localIp;
        }

        String ip = doGetIp();
        if (Objects.nonNull(ip)){
            localIp = ip;
        }
        return localIp;
    }

    private static String doGetIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("[IpUtils#doGetIp] getIp error!",e);
            return null;
        }
    }
}
