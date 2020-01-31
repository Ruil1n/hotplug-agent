package cn.rui0.javaagent.weavers.asm.transformer;

import cn.rui0.javaagent.constants.AopClassAndMethos;
import cn.rui0.javaagent.utils.ClassUtils;
import cn.rui0.javaagent.utils.JvmUtils;
import cn.rui0.javaagent.weavers.asm.MyClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;


/**
 * Created by ruilin on 2019/7/10.
 */
public class InjectTransformer implements ClassFileTransformer {
    private String pid;

    public InjectTransformer(String pid) {
        this.pid = pid;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] returnByte = null;

        //check class
        String classNameJava = JvmUtils.jvmnameToJavaname(className);
        if (!matchClazz(classNameJava)) {
            return returnByte;
        }

        ClassUtils.storeClassFile("/tmp/agent-demo/class/" + pid, className, classfileBuffer);


        System.out.println("Transforming start " + className);
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        MyClassVisitor myClassVisitor = new MyClassVisitor(classWriter);
        classReader.accept(myClassVisitor, 0);
        returnByte = classWriter.toByteArray();


        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CheckClassAdapter.verify(new ClassReader(returnByte), false, pw);
        ClassUtils.storeClassFile("/tmp/agent-demo/class_modified/" + pid, className, returnByte);

        return returnByte;
    }

    private static boolean matchClazz(String clazzName) {
        //System.out.println(clazz.getName());
        return clazzName.equalsIgnoreCase(AopClassAndMethos.springMVCClass);
    }


}
