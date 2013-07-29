package org.pepper.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// ex. @Given("x is $value")
@Retention(RetentionPolicy.RUNTIME) // indicates annotation will be available at runtime
@Target(ElementType.METHOD)         // indicates annotation can only be applied to methods
public @interface Given {
  public String value();
}
