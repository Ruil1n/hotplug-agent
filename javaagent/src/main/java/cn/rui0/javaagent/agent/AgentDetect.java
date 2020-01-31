package cn.rui0.javaagent.agent;

import cn.rui0.javaagent.classloader.SandboxClassLoader;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AgentDetect {
    public static Class<?> classOfagent;
    public static final String CLOSE_COMMAND = "REDUCTION;";
    // 全局持有ClassLoader用于隔离sandbox实现
    private static volatile Map<String/*NAMESPACE*/, SandboxClassLoader> sandboxClassLoaderMap
            = new ConcurrentHashMap<String, SandboxClassLoader>();

    // test
    private static final String TEST_AGENT = "/Users/ruilin/test/agent/javaagent/target"+ File.separator + "javaagent-1.0-SNAPSHOT-jar-with-dependencies.jar";


    public static void premain(String args, Instrumentation instr) {
        System.out.println("Premain 方法执行，参数args为：" + args);
    }


    public static void agentmain(String args, Instrumentation instr) throws Throwable {

        String namespace = args;//pid
        System.out.println("pid:" + namespace);
        // 构造自定义的类加载器，尽量减少Sandbox对现有工程的侵蚀
        if (classOfagent == null) {
            ClassLoader sandboxClassLoader = loadOrDefineClassLoader(
                    namespace,
                    "javaagent-1.0-SNAPSHOT-release.jar"
                    // SANDBOX_CORE_JAR_PATH
            );
            classOfagent = sandboxClassLoader.loadClass("cn.rui0.javaagent.agent.Agent");
        }
        System.out.println("Loaded by :" + classOfagent.getClassLoader());
        Method mainMethod = classOfagent.getMethod("agent", String.class, Instrumentation.class);
        mainMethod.invoke(null, namespace, instr);

        if (namespace.startsWith(CLOSE_COMMAND))
            uninstall(namespace.replace(CLOSE_COMMAND,""));

    }

    private static synchronized ClassLoader loadOrDefineClassLoader(final String namespace,
                                                                    final String coreJar) throws Throwable {

        final SandboxClassLoader classLoader;

        // 如果已经被启动则返回之前启动的ClassLoader
        if (sandboxClassLoaderMap.containsKey(namespace)
                && null != sandboxClassLoaderMap.get(namespace)) {
            classLoader = sandboxClassLoaderMap.get(namespace);
        }

        // 如果未启动则重新加载
        else {
            classLoader = new SandboxClassLoader(namespace, coreJar);
            sandboxClassLoaderMap.put(namespace, classLoader);
        }

        return classLoader;
    }


    /**
     * 删除指定命名空间下的jvm-sandbox
     *
     * @param namespace 指定命名空间
     * @throws Throwable 删除失败
     */
    @SuppressWarnings("unused")
    public static synchronized void uninstall(final String namespace) throws Throwable {
        final SandboxClassLoader sandboxClassLoader = sandboxClassLoaderMap.get(namespace);
        if (null == sandboxClassLoader) {
            System.out.println("\n"+namespace+" sandboxClassLoader is null");
            return;
        }

        System.out.println("\n"+namespace+" uninstall:"+sandboxClassLoader.toString());
        // 关闭SandboxClassLoader
        sandboxClassLoader.closeIfPossible();
        // 去除引用 否则无法回收
        classOfagent=null;
        sandboxClassLoaderMap.remove(namespace);
        System.out.println("end uninstall");
    }

}
