package cn.rui0.javaagent.agent;

import cn.rui0.javaagent.constants.AopClassAndMethos;
import cn.rui0.javaagent.weavers.asm.transformer.InjectTransformer;
import cn.rui0.javaagent.weavers.asm.transformer.RestoreTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;


import static cn.rui0.javaagent.JVMThread.CLOSE_COMMAND;

/**
 * Created by ruilin on 2019/7/11.
 */
public class Agent {
    public static ClassFileTransformer injectTransformer;
    public static ClassFileTransformer restoreTransformer;
    public static List<Instrumentation> instrumentations = new ArrayList<>();


    public static void agent(String args, Instrumentation instr){


        System.out.println("Args is " + args);
        System.err.println("====Begin to retransformClasses====");
        // 添加ClassFileTransformer
        try {
            // JavaassistClassFileTransformer自定义的实现ClassFileTransformer的类
            // instr.addTransformer(new JavaassistClassFileTransformer(), true);

            String pid = args.replace(CLOSE_COMMAND, "");

            // 如果终止时需要卸载
            if (args.startsWith(CLOSE_COMMAND)){
                if (injectTransformer!=null) {
                    //instrumentations.forEach(t -> t.removeTransformer(injectTransformer));
                    for (Instrumentation i:instrumentations) {
                        i.removeTransformer(injectTransformer);
                    }
                    injectTransformer=null;
                    System.err.println("remove InjectTransformer");
                }
                System.out.println("add RestoreTransformer , pid:  " + pid);
                restoreTransformer=new RestoreTransformer(pid);
                instr.addTransformer(restoreTransformer, true);
                instrumentations.add(instr);

            }else {
                System.out.println("add InjectTransformer , pid:  " + pid);
                injectTransformer=new InjectTransformer(pid);
                instr.addTransformer(injectTransformer, true);
                instrumentations.add(instr);
            }

            Class[] classes = instr.getAllLoadedClasses();
            for (Class clazz : classes) {
                // 对指定的类进行transform
                if (matchClazz(clazz)) {
                    System.out.println("Class matched " + clazz.getName());
                    instr.retransformClasses(clazz);
                    if (restoreTransformer!=null) {
                        //instrumentations.forEach(t -> t.removeTransformer(restoreTransformer));
                        for (Instrumentation i:instrumentations) {
                            i.removeTransformer(restoreTransformer);
                        }
                        restoreTransformer=null;
                        System.err.println("remove RestoreTransformer");
                    }
                }
            }
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
        System.err.println("====End to retransformClasses====");
    }
    private static boolean matchClazz(Class clazz) {
        return clazz.getName().equalsIgnoreCase(AopClassAndMethos.springMVCClass);
    }

}
