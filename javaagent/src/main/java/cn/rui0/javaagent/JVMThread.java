package cn.rui0.javaagent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;


/**
 * javaagent热插拔demo
 * By Ruilin
 */
public class JVMThread {
    public static final String CLOSE_COMMAND = "REDUCTION;";

    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        if (args.length != 1) {
            System.err.println("must point pid");
            return;
        }
        String pid=args[0];
        JVMThread.doShutDownWork(pid);

        VirtualMachine virtualMachine = VirtualMachine.attach(pid);
        String os = System.getProperty("os.name");
        String agentJarPath = JVMThread.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println(agentJarPath);
        if (os.toLowerCase().startsWith("win")) {//windows文件路径中开始会有/
            agentJarPath = agentJarPath.substring(1);
        }
        virtualMachine.loadAgent(agentJarPath, pid);
        virtualMachine.detach();
        System.out.println("attach running ...");
        System.out.println(getDefaultClassLoader().toString());
        while (true) ;

    }
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
        }
        if (cl == null) {
            cl = JVMThread.class.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                }
            }
        }
        return cl;
    }


    private static void doShutDownWork(String namespace) {

        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            try {
                VirtualMachine virtualMachine = VirtualMachine.attach(namespace);
                String agentJarPath = JVMThread.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                String os = System.getProperty("os.name");
                if (os.toLowerCase().startsWith("win")) {//windows文件路径中开始会有/
                    agentJarPath = agentJarPath.substring(1);
                }
                virtualMachine.loadAgent(agentJarPath, CLOSE_COMMAND + namespace);
                virtualMachine.detach();

                runtime.gc();
            } catch (AgentLoadException e) {
                e.printStackTrace();
            } catch (AgentInitializationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AttachNotSupportedException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            System.out.println("\nREDUCTION!");
        }));
    }


}
