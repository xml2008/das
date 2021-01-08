package com.ppdai.das.tx.process;

import com.google.common.base.Preconditions;
import com.ppdai.das.tx.NodeMeta;
import com.ppdai.das.tx.annotation.DasTransactional;
import com.ppdai.das.tx.monitor.BusEventManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class DasTransactionalBeanPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final Class<?> clazz = AopUtils.isAopProxy(bean)? AopUtils.getTargetClass(bean) : bean.getClass();

        Method[] methods = clazz.getMethods();
        Stream.of(methods)
                .filter(method -> Objects.nonNull(method.getAnnotation(DasTransactional.class)))
                .forEach(method -> {
                    try {
                        saveNodeMeta(bean, clazz, method);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                });

        return bean;
    }


    static Map<String, NodeMeta> metas = new HashMap<>();

    public static Map<String, NodeMeta> getMetas() {
        return metas;
    }

    private void saveNodeMeta(Object bean, Class clz, Method method) throws NoSuchMethodException {
        DasTransactional annotation = method.getAnnotation(DasTransactional.class);
        Preconditions.checkArgument(annotation.retry() >= 0);

        NodeMeta nodeMeta = new NodeMeta();
        nodeMeta.setTarget(bean);

        if(annotation.confirmMethod().equals("")) {
            nodeMeta.setConfirmMethod(clz.getMethod(method.getName()+"Confirm"));
        } else {
            nodeMeta.setConfirmMethod(clz.getMethod(annotation.confirmMethod()));
        }

        if(annotation.cancelMethod().equals("")) {
            nodeMeta.setCancelMethod(clz.getMethod(method.getName()+"Cancel"));
        } else {
            nodeMeta.setCancelMethod(clz.getMethod(annotation.cancelMethod()));
        }

        nodeMeta.setTryMethod(clz.getMethod(method.getName()));
        metas.put(method.getName(), nodeMeta);
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
