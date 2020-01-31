package cn.rui0.javaagent.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */

public class ClassUtils {
    private static final Logger logger = LogManager.getLogger(ClassUtils.class.getName());

    public static String getPackagePath(String className) {
        String packagePath = "";
        String[] list = className.split("\\/");
        for (int i = 0; i < list.length - 1; i++) {
            packagePath += list[i] + "/";
        }
        return packagePath;

    }

    /**
     * className is separated by dot(".")
     * @param prefix
     * @param className
     * @return
     */
    public static String getLocalPath(String prefix, String className) {
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        return prefix + className.replace(".", "/") + ".class";
    }

    /**jps
     * save class file
     * @param path
     * @param className className with slash : com/alipay/example
     * @param classFile
     */
    public static void storeClassFile(String path, String className, byte[] classFile) {
        try {
            if (!path.endsWith("/")) {
                path += "/";
            }
            File dir = new File(path + getPackagePath(className));
            if (dir.exists() && dir.isFile()) {
                logger.info("dir is exist");
            } else {
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(path + className + ".class");
                FileOutputStream stream = new FileOutputStream(file);
                stream.write(classFile);
                stream.close();
            }
        } catch (Exception e) {
            logger.error("store class encounter Exception", e);
        }
    }

    public static void main(String[] args) {
        String className = "com/alipay/lock/processor/policy/HasLockPolicy";
        System.out.println(ClassUtils.getPackagePath(className));
    }
}
