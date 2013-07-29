package org.pepper.core.models;

import java.lang.reflect.Method;

import org.pepper.core.annotations.When;

public class WhenFrameworkMethod extends StepMethod {

  public WhenFrameworkMethod(Method method) {
    super(method);
  }

  @Override
  public String getStep() {
    return "When " + (getAnnotation(When.class)).value();
  }
}
