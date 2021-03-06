package org.pepper.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// ex @When("I multiply x by $value")
@Retention(RetentionPolicy.RUNTIME) // indicates annotation will be available at runtime
@Target(ElementType.METHOD)         // indicates annotation can only be applied to methods
public @interface When {
  public String value();
}
