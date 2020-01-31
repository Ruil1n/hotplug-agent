package cn.rui0.javaagent.weavers.asm;


import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.MethodVisitor;

public class MyMethodVisitor extends AdviceAdapter {
    public MyMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
    }

    /**
     * 进入
     */
    @Override
    public void onMethodEnter() {
        super.onMethodEnter();
        this.visitMethodInsn(INVOKESTATIC, "cn/rui0/javaagent/weavers/asm/InvokeTimer", "start", "()V", false);
    }

    /**
     * 退出
     *
     * @param opcode
     */
    @Override
    public void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        mv.visitMethodInsn(INVOKESTATIC, "cn/rui0/javaagent/weavers/asm/InvokeTimer", "end", "()V", false);
    }


}
