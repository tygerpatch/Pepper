package org.pepper.core.models;

import java.lang.reflect.Method;

import org.pepper.core.annotations.Then;

public class ThenFrameworkMethod extends StepMethod {

  public ThenFrameworkMethod(Method method) {
    super(method);
  }

  @Override
  public String getStep() {
    return "Then " + (getAnnotation(Then.class)).value();
  }
}
