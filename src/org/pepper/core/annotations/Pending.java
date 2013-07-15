package org.pepper.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// ex.
// @Pending
// @Given("a variable x with value 3")
// public void givenAVariableXWithValue3() {
// }
@Retention(RetentionPolicy.RUNTIME) // indicates annotation will be available at runtime
@Target(ElementType.METHOD)         // indicates annotation can only be applied to methods
public @interface Pending {
}
