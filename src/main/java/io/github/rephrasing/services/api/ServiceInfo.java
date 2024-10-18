package io.github.rephrasing.services.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceInfo {
    String value();
    boolean async() default false;
    Class<? extends Service>[] dependsOn() default {};
}