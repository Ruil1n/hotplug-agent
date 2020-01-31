package cn.rui0.javaagent.weavers.javaassist;


import cn.rui0.javaagent.constants.AopClassAndMethos;
import cn.rui0.javaagent.utils.JvmUtils;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;


public class JavaassistClassFileTransformer implements ClassFileTransformer {

    private ClassPool classPool = ClassPool.getDefault();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //return new byte[0];
        System.out.println("Transforming start " + className);
        CtClass ctClass = null;

        byte[] returnByte = null;

        String classNameJava = JvmUtils.jvmnameToJavaname(className);
        if (!matchClazz(classNameJava)) {
            return null;
        }

        try {
            ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            System.out.println("ctClass.getName() " + ctClass.getName());
            if (!ctClass.isInterface()) {
                for(CtBehavior method : ctClass.getDeclaredBehaviors()) {
                    if (matchMethod(method.getLongName())) {
                        System.out.println("[JavaassistClassFileTransformer]Transforming className = " + className);
                        System.out.println("Transforming class long method name " + method.getLongName());
                        method.insertBefore(insertBeforStr(method));
                        method.insertAfter(insertAfterStr(method));
                        System.out.println("CtClass.toString() -1 \n" + ctClass.toString());
                    }

                }
                System.out.println("CtClass.toString() -2 \n " + ctClass.toBytecode().toString());
                returnByte = ctClass.toBytecode();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } finally {
            if (ctClass != null) {
                ctClass.detach();
            }
        }

        System.out.println("Transforming end");
        return returnByte;
    }


    private boolean matchMethod(String methodName) {
        return methodName.equalsIgnoreCase(AopClassAndMethos.springMVCMethod);
    }

    private String insertBeforStr(CtBehavior method) throws CannotCompileException {
        System.out.println("insertbefor satrt " + method.getMethodInfo().toString());
        method.addLocalVariable("startTime", CtClass.longType);
        StringBuilder stringBuilder = new StringBuilder();
//         stringBuilder.append("System.out.println(\"Enter \"" + method.getClass().getName() + "\".\"" + method.getLongName() + ");");
         stringBuilder.append("System.out.println(\"Start at : \" + System.currentTimeMillis());");
         stringBuilder.append("startTime = System.currentTimeMillis();");
        stringBuilder.append("System.out.println(\"Method Start\");");
        stringBuilder.append("System.out.println(\"startTime \" + startTime);");
        return stringBuilder.toString();
    }

    private String insertAfterStr(CtBehavior method) {
        System.out.println("insertafter start " + method.getMethodInfo().toString());
        StringBuilder stringBuilder = new StringBuilder();
//         stringBuilder.append("System.out.println(\"Leave\"" + method.getClass().getName() + "\".\"" + method.getLongName() + ");");
//         stringBuilder.append("System.out.println(\"Cost \" + System.currentTimeMillis() - startTime);");
        stringBuilder.append("System.out.println(\"Method End\");");
        return stringBuilder.toString();
    }

    private static boolean matchClazz(String clazzName) {
        //System.out.println(clazz.getName());
        return clazzName.equalsIgnoreCase(AopClassAndMethos.springMVCClass);
    }


}
