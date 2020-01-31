package cn.rui0.javaagent.classloader;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 自定义的ClassLoader
 */
public class SandboxClassLoader extends URLClassLoader{

    private final String namespace;
    private final String path;
    //private static Map<String, byte[]> classMap = new ConcurrentHashMap<>();

    public SandboxClassLoader(final String namespace,
                       final String sandboxCoreJarFilePath) throws MalformedURLException {
        super(new URL[]{new URL("file:" + sandboxCoreJarFilePath)});
        this.namespace = namespace;
        this.path = sandboxCoreJarFilePath;
//        preReadClassFile();
//        preReadJarFile();
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if(null != url) {
            return url;
        }
        url = super.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = findResources(name);
        if( null != urls ) {
            return urls;
        }
        urls = super.getResources(name);
        return urls;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // 优先从parent（SystemClassLoader）里加载系统类，避免抛出ClassNotFoundException
        if(name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
            return super.loadClass(name, resolve);
        }
        if (name.startsWith("cn.rui0"))
            System.out.println(name);
        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            return super.loadClass(name, resolve);
        }
    }

    @Override
    public String toString() {
        return String.format("SandboxClassLoader[namespace=%s;path=%s;]", namespace, path);
    }


    /**
     * 尽可能关闭ClassLoader
     * <p>
     * URLClassLoader会打开指定的URL资源，在SANDBOX中则是对应的Jar文件，如果不在shutdown的时候关闭ClassLoader，会导致下次再次加载
     * 的时候，依然会访问到上次所打开的文件（底层被缓存起来了）
     * <p>
     * 在JDK1.7版本中，URLClassLoader提供了{@code close()}方法来完成这件事；但在JDK1.6版本就要下点手段了；
     * <p>
     * 该方法将会被{@code ControlModule#shutdown}通过反射调用，
     * 请保持方法声明一致
     */
    //@SuppressWarnings("unused")
    public void closeIfPossible() {

        // 如果是JDK7+的版本, URLClassLoader实现了Closeable接口，直接调用即可
        if (this instanceof Closeable) {
            try {
                final Method closeMethod = URLClassLoader.class.getMethod("close");
                closeMethod.invoke(this);
            } catch (Throwable cause) {
                // ignore...
            }
            return;
        }


        // 对于JDK6的版本，URLClassLoader要关闭起来就显得有点麻烦，这里弄了一大段代码来稍微处理下
        // 而且还不能保证一定释放干净了，至少释放JAR文件句柄是没有什么问题了
        try {
            final Object sun_misc_URLpath = URLClassLoader.class.getDeclaredField("ucp").get(this);
            final Object java_util_Collection = sun_misc_URLpath.getClass().getDeclaredField("loaders").get(sun_misc_URLpath);

            for (Object sun_misc_URLpath_JarLoader :
                    ((Collection) java_util_Collection).toArray()) {
                try {
                    final JarFile java_util_jar_JarFile = (JarFile) sun_misc_URLpath_JarLoader.getClass().getDeclaredField("jar").get(sun_misc_URLpath_JarLoader);
                    java_util_jar_JarFile.close();
                } catch (Throwable t) {
                    // if we got this far, this is probably not a JAR loader so skip it
                }
            }

        } catch (Throwable cause) {
            // ignore...
        }

    }

/////////
    /*
    public static boolean addClass(String className, byte[] byteCode) {
        if (!classMap.containsKey(className)) {
            classMap.put(className, byteCode);
            return true;
        }
        return false;
    }
    */

    /*
     * 这里仅仅卸载了myclassLoader的classMap中的class,虚拟机中的
     * Class的卸载是不可控的
     * 自定义类的卸载需要MyClassLoader不存在引用等条件
     * @param className
     * @return
     */
/////////
    /*
    public static boolean unloadClass(String className) {
        if (classMap.containsKey(className)) {
            classMap.remove(className);
            return true;
        }
        return false;
    }
    */

//    /**
//     * 遵守双亲委托规则
//     */
//    @Override
//    protected Class<?> findClass(String name) {
//        try {
//            byte[] result = getClass(name);
//            if (result == null) {
//                throw new ClassNotFoundException();
//            } else {
//                return defineClass(name, result, 0, result.length);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /////////
    /*
    private byte[] getClass(String className) {
        if (classMap.containsKey(className)) {
            return classMap.get(className);
        } else {
            return null;
        }
    }

    private void preReadClassFile() {
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                scanClassFile(file);
            }
        }
    }

    private void scanClassFile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                try {
                    byte[] byteCode = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                    String className = file.getAbsolutePath().replace(path, "")
                            .replace(File.separator, ".")
                            .replace(".class", "");
                    addClass(className, byteCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    scanClassFile(f);
                }
            }
        }
    }

    private void preReadJarFile() {
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                scanJarFile(file);
            }
        }
    }

    private void readJAR(JarFile jar) throws IOException {
        Enumeration<JarEntry> en = jar.entries();
        while (en.hasMoreElements()) {
            JarEntry je = en.nextElement();
            je.getName();
            String name = je.getName();
            if (name.endsWith(".class")) {
                //String className = name.replace(File.separator, ".").replace(".class", "");
                String className = name.replace("\\", ".")
                        .replace("/", ".")
                        .replace(".class", "");
                System.out.println("read"+className+"in jar");
                InputStream input = null;
                ByteArrayOutputStream baos = null;
                try {
                    input = jar.getInputStream(je);
                    baos = new ByteArrayOutputStream();
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int bytesNumRead = 0;
                    while ((bytesNumRead = input.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesNumRead);
                    }
                    addClass(className, baos.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (baos != null) {
                        baos.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                }
            }
        }
    }

    private void scanJarFile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    readJAR(new JarFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    scanJarFile(f);
                }
            }
        }
    }


    public void addJar(String jarPath) throws IOException {
        File file = new File(jarPath);
        if (file.exists()) {
            JarFile jar = new JarFile(file);
            readJAR(jar);
        }
    }
    */
}
