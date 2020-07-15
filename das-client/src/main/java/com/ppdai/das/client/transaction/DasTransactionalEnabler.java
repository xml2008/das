package com.ppdai.das.client.transaction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.base.Preconditions;
import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.client.Hints;
import com.ppdai.das.client.annotation.DasTransactional;
import com.ppdai.das.client.annotation.DefaultShard;
import com.ppdai.das.client.annotation.Shard;
import com.ppdai.das.core.DasException;
import com.ppdai.das.core.client.DalTransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class DasTransactionalEnabler{

    @Around(value="@annotation(DasTransactional)")
    public Object around(ProceedingJoinPoint joinPoint, DasTransactional DasTransactional) throws Throwable {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object result = (Object)intercept(method, args, joinPoint);
        return result;
    }

    private Object intercept( final Method method, final Object[] args, ProceedingJoinPoint joinPoint  ) throws Throwable {
        Hints hints = null;

        for(Object o: args) {
            if(o instanceof Hints) {
                hints = (Hints) o;
            }
        }

        Annotation[][] paraAnnArrays = method.getParameterAnnotations();
        int shardParaIndex = -1;
        int defaultShardParaIndex = -1;
        int i = 0;
        outter: for(Annotation[] paraAnnArray: paraAnnArrays) {
            for(Annotation paraAnn: paraAnnArray) {
                if(paraAnn instanceof DefaultShard) {
                    defaultShardParaIndex = i;
                    break outter;
                }
                if(paraAnn instanceof Shard) {
                    shardParaIndex = i;
                    break outter;
                }
            }
            i++;
        }

        hints = hints == null ? new Hints():hints.clone();

        if(shardParaIndex != -1) {
            Object shard = args[shardParaIndex];
            if(shard != null) {
                hints.inShard(shard.toString());
            }
        }
        if(defaultShardParaIndex != -1) {
            Object shard = args[defaultShardParaIndex];
            if(shard != null){
                checkMultipleDefaultShard(shard.toString());
                hints.inShard(shard.toString()).applyDefaultShard();
            }
        }

        final AtomicReference<Object> result = new AtomicReference<>();
        if(getRollback(method)) {
            hints.rollbackOnly();
        }
        DasClientFactory.getClient(getLogicDbName(method)).execute(() -> {
            try {
                result.set(joinPoint.proceed(args));
            } catch (Throwable e) {
                throw DasException.wrap(e);
            }
        }, hints);
        return result.get();
    }

    private void checkMultipleDefaultShard(String shard) {
        if(DalTransactionManager.isDefaultShardApplied() && DalTransactionManager.getCurrentShardId() != null) {
            Preconditions.checkArgument(DalTransactionManager.getCurrentShardId().equals(shard),
                    "@DefaultShard are not same: [" + DalTransactionManager.getCurrentShardId() + "], [" + shard + "]");
        }
    }

    private String getLogicDbName(Method method) {
        DasTransactional tran = method.getAnnotation(DasTransactional.class);
        if(tran != null) {
            return tran.logicDbName();
        }

        return method.getAnnotation(DasTransactional.class).logicDbName();
    }

    private boolean getRollback(Method method) {
        DasTransactional tran = method.getAnnotation(DasTransactional.class);
        if(tran != null) {
            return tran.rollback();
        }

        return method.getAnnotation(DasTransactional.class).rollback();
    }

}