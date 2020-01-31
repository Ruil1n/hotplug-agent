package cn.rui0.javaagent.weavers.asm;


import cn.rui0.javaagent.constants.AopClassAndMethos;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class MyClassVisitor extends ClassVisitor {
    private boolean isInterface;

    public MyClassVisitor(ClassVisitor classVisitor) {
        super(ASM7, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        System.out.println("visitMethod:" + name);
        if (!isInterface && methodVisitor != null && matchMethod(name)) {
            System.out.println("EnterMethod:" + name);
            methodVisitor = new MyMethodVisitor(ASM7, methodVisitor, access, name, desc);
        }
        return methodVisitor;
    }

    private static boolean matchMethod(String methodName) {
        return methodName.equalsIgnoreCase(AopClassAndMethos.springMVCMethodForASM);
    }
}
