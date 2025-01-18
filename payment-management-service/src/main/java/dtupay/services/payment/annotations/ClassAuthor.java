package dtupay.services.payment.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ClassAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClassAuthor {
  String author();
  String stdno() default "";
}