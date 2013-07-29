package org.pepper.core.models;

import java.lang.reflect.Method;

import org.pepper.core.annotations.Then;

public class ThenStepMethod extends StepMethod {

  public ThenStepMethod(Method method) {
    super(method);
  }

  @Override
  public String getStep() {
    return "Then " + (getAnnotation(Then.class)).value();
  }
}
