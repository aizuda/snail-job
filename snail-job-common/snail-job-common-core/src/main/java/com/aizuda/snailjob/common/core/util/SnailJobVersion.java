package com.aizuda.snailjob.common.core.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * 获取系统版本
 *
 * @author: opensnail
 * @date : 2023-05-22 08:55
 */
public final class SnailJobVersion {

    private SnailJobVersion() {
    }

    /**
     * 获取 Snail Job 最新版本号
     */
    public static String getVersion() {
        return determineSpringBootVersion();
    }

    private static String determineSpringBootVersion() {
        String implementationVersion = SnailJobVersion.class.getPackage().getImplementationVersion();
        if (implementationVersion != null) {
            return implementationVersion;
        }
        CodeSource codeSource = SnailJobVersion.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return null;
        }
        URL codeSourceLocation = codeSource.getLocation();
        try {
            URLConnection connection = codeSourceLocation.openConnection();
            if (connection instanceof JarURLConnection) {
                return getImplementationVersion(((JarURLConnection) connection).getJarFile());
            }
            try (JarFile jarFile = new JarFile(new File(codeSourceLocation.toURI()))) {
                return getImplementationVersion(jarFile);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private static String getImplementationVersion(JarFile jarFile) throws IOException {
        return jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
    }

}
