package com.ppdai.das.tx.annotation;

import com.ppdai.das.tx.TxTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DasTransactional {
    TxTypeEnum type();

    String confirmMethod() default  "";

    String cancelMethod() default  "";

    int retry() default 0;
}
