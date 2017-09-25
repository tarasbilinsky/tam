package base.models.annotations;


import base.models.enums.FormatType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface  FieldFormat {
    FormatType value() default FormatType.Undefined;
    String param() default "";
}
