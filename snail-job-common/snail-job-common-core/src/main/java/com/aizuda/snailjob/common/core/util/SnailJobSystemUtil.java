package com.aizuda.snailjob.common.core.util;

public class SnailJobSystemUtil {

    public static String getOsName(){
        return System.getProperty("os.name");
    }

    public static boolean isOsWindows(){
        String osName = getOsName();
        return osName != null && osName.toLowerCase().contains("windows");
    }
}
