package minispringframework.annotation;

@Component
public @interface Controller {
    String value() default "";
}
