package cn.rui0.javaagent.weavers.asm.transformer;

import cn.rui0.javaagent.constants.AopClassAndMethos;
import cn.rui0.javaagent.utils.ClassUtils;
import cn.rui0.javaagent.utils.JvmUtils;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class RestoreTransformer implements ClassFileTransformer {
    private String pid;

    public RestoreTransformer(String pid) {
        this.pid = pid;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        String classNameJava = JvmUtils.jvmnameToJavaname(className);
        if (!matchClazz(classNameJava)) {
            return null;
        }
        System.err.println(className + ":start to restore");
        String filePath = ClassUtils.getLocalPath("/tmp/agent-demo/class/" + pid, className);
        InputStream is;
        try {
            is = new FileInputStream(new File(filePath));
            ClassReader cr = new ClassReader(is);

            return cr.b;

        } catch (Exception e) {
            System.err.println("Encounter Exception");
            e.printStackTrace();
        }


        return null;
    }

    private static boolean matchClazz(String clazzName) {
        //System.out.println(clazz.getName());
        return clazzName.equalsIgnoreCase(AopClassAndMethos.springMVCClass);
    }
}
