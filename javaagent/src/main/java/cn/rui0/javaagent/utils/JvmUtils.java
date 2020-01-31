package cn.rui0.javaagent.utils;

public class JvmUtils {

    public static String jvmnameToJavaname(String jvmName) {
        if (jvmName == null) {
            throw new NullPointerException("jvmName must not be null");
        }
        return jvmName.replace('/', '.');
    }
}
