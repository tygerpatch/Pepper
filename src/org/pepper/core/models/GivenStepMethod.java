package org.pepper.core.models;

import java.lang.reflect.Method;

import org.pepper.core.annotations.Given;

public class GivenStepMethod extends StepMethod {

  public GivenStepMethod(Method method) {
    super(method);
  }

  @Override
  public String getStep() {
    return "Given " + (getAnnotation(Given.class)).value();
  }
}
