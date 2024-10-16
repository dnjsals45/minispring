package study.minispringframework.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Controller {
    @AliasFor(
            annotation = Component.class
    )

    String value() default "";
}
