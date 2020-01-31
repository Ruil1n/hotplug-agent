package cn.rui0.springboot.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionLogAspect {

    @Pointcut("execution(* cn.rui0.springboot.service..*.*(..))")
    public void exceptionLogPointCut() { }


    @AfterThrowing(value = "exceptionLogPointCut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Exception ex) {
        // 代表方法的签名
        Signature signature = joinPoint.getSignature();
        System.out.println("拦截的方法名为：" + signature.getName());
        System.out.println("异常信息为：");
        ex.printStackTrace(System.out);
        System.out.println("异常信息打印完毕");
    }

}
