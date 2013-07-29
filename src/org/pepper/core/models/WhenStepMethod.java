package org.pepper.core.models;

import java.lang.reflect.Method;

import org.pepper.core.annotations.When;

public class WhenStepMethod extends StepMethod {

  public WhenStepMethod(Method method) {
    super(method);
  }

  @Override
  public String getStep() {
    return "When " + (getAnnotation(When.class)).value();
  }
}
