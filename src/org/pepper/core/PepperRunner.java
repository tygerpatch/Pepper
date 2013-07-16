package org.pepper.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.pepper.core.annotations.Given;
import org.pepper.core.annotations.Pending;
import org.pepper.core.annotations.Then;
import org.pepper.core.annotations.When;

public class PepperRunner extends BlockJUnit4ClassRunner {

  public PepperRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  private StepDefinition stepDef;
  private String path;

  private void newStepDefinition() {
    try {
      stepDef = (StepDefinition) getTestClass().getOnlyConstructor().newInstance();

      // This is where you can pass configuration information from the StepDefinition to the PepperRunner.
      if (path == null) {
        path = new File(stepDef.getFeaturesFolder() + "/" + stepDef.getFeatureName() + stepDef.getFeatureExtension()).getAbsolutePath();
      }
    }
    catch (InvocationTargetException invocationTarget) {
      invocationTarget.printStackTrace();
    }
    catch (IllegalAccessException illegalAccess) {
      illegalAccess.printStackTrace();
    }
    catch (InstantiationException instantiation) {
      instantiation.printStackTrace();
    }
  }

  // Given a line in the feature file, it returns the proper Step method in the StepDefinitions file to call
  public FrameworkMethod extractMethod(String line) {
    List<FrameworkMethod> methodList = map.get(line);
    params.clear();

    // *** handle non-parameterized Given-When-Then step
    if((methodList != null) && (methodList.size() == 1)) {
      return methodList.get(0);
    }

    // *** look for a parameterized version, with the correct type of parameters
    // Note: Users should expect parameterized steps to take a little longer to process.

    StringTokenizer stepTokenizer, keyTokenizer;
    String keyToken, stepToken;

    // for each defined step
    for (String key : map.keySet()) {

      params.clear();

      // iterate over each word in the step method and in the line of the feature file
      keyTokenizer = new StringTokenizer(key);
      stepTokenizer = new StringTokenizer(line);

      while (keyTokenizer.hasMoreTokens() && stepTokenizer.hasMoreTokens()) {
        keyToken = keyTokenizer.nextToken();
        stepToken = stepTokenizer.nextToken();

        // if the word in the given line of the feature file doesn't match up with the word in the step
        if (!keyToken.equals(stepToken)) {
          if (!keyToken.startsWith("$")) {
            break; // from while loop
          }
          // if word in step method begins with $ then it must be variable placeholder
          if (keyToken.startsWith("$")) {
            try {
              params.add(Integer.valueOf(stepToken));
            }
            catch (NumberFormatException intNumberFormat) {
              // TODO: should exception be logged
              try {
                params.add(Double.valueOf(stepToken));
              }
              catch (NumberFormatException dblNumberFormat) {
                // TODO: should exception be logged
                params.add(Boolean.valueOf(stepToken));
              }
            }
          }
        }
      } // end of while loop

      // if there are no more words in either the line or the step method
      if (!keyTokenizer.hasMoreTokens() && !stepTokenizer.hasMoreTokens()) {
        Method method;
        Class<?>[] parameterTypes;

        // for each of the FrameworkMethod(s) associated with this step
        withNextMethod:
        for(FrameworkMethod frameworkMethod : map.get(key)) {

          // get info the method associated with this step
          method = frameworkMethod.getMethod();
          parameterTypes = method.getParameterTypes();

          // ensure that parameter types match up
          for(int i = 0; (i < params.size()) && (i < parameterTypes.length); i++) {
            switch(parameterTypes[i].getCanonicalName()) {
              case "int":
                if(!"class java.lang.Integer".equalsIgnoreCase(params.get(i).getClass().toString())) {
                  continue withNextMethod;
                }
                break;
              case "double":
                if(!"class java.lang.Double".equalsIgnoreCase(params.get(i).getClass().toString())) {
                  continue withNextMethod;
                }
                break;
              case "boolean":
                if(!"class java.lang.Boolean".equalsIgnoreCase(params.get(i).getClass().toString())) {
                  continue withNextMethod;
                }
                break;
            }
          }

          return frameworkMethod;
        }
      }
    } // end for (String key : map.keySet()) {

    return null;
  }

  public void generateStub(String line) {
    System.out.println("@Pending");

    // Given = 5 characters long
    // When = 4 characters long
    // Then = 4 characters long
    int length = line.startsWith("Given") ? 5 : 4;
    System.out.println("@" + line.substring(0, length) + "(\"" + line.substring(length + 1) + "\")");

    System.out.println("public void " + StringUtils.camelCase(line) + "() {");
    System.out.println("}");
    System.out.println();

    // TODO: this could easily be made testable by returning a String containing the output
    // TODO: write test to ensure someone doesn't do something like "giventhat x is 3"
  }

  // Invokes Step methods in StepDefinition
  @Override
  protected Statement childrenInvoker(final RunNotifier notifier) {

    newStepDefinition();
    final PepperRunnerListener listener = new PepperRunnerListener();
    notifier.addListener(listener);

    return new Statement() {
      @Override
      public void evaluate() {
        try {
          File file = new File(path);
          Scanner scanner = new Scanner(file); // <- FileNotFoundException
          String line;
          FrameworkMethod method;

          while (scanner.hasNextLine()) {
            line = scanner.nextLine().trim();

            if (line.startsWith("Scenario:")) {
              newStepDefinition();
              System.out.println(line);
            }
            else if (line.startsWith("Given") || line.startsWith("When") || line.startsWith("Then")) {
              method = extractMethod(line);

              if (method == null) {
                generateStub(line);
              }
              else {
                listener.setLine(line);
                PepperRunner.this.runChild(method, notifier);
              }
            }
            else {
              System.out.println(line);
            }
          }
          scanner.close();
        }
        catch (FileNotFoundException fileNotFound) {
          fileNotFound.printStackTrace();
        }
      }
    };
  }

  @Deprecated
  @Override
  protected void validateInstanceMethods(List<Throwable> errors) {
    // This method is called by collectInitializationErrors(List<Throwable> errors),
    // which is called by validate() method,
    // which is called inside ParentRunner's ParentRunner(Class<?> testClass) constructor.

    // For some strange reason the given-when-then maps aren't initialized when this method is called.
    // The JUnit API says this method will go away in the future. So I think it's safe to comment out.
  }

  private Map<String, List<FrameworkMethod>> map = new HashMap<String, List<FrameworkMethod>>();

  // computeTestMethod is called by getChildren
  // getChildren is an overridden method
  // computeTestMethod is not overridden

//  @Override
//  protected List<FrameworkMethod> getChildren() {
//    // return computeTestMethods();
//    // --
//    // TODO: create tests for this method
//    List<FrameworkMethod> list = new ArrayList<FrameworkMethod>();
//    TestClass testClass = this.getTestClass();
//
//    for (FrameworkMethod method : testClass.getAnnotatedMethods(Given.class)) {
//      if(method.getAnnotation(Pending.class) != null) {
//        list.add(method);
//      }
//    }
//
//    for (FrameworkMethod method : testClass.getAnnotatedMethods(When.class)) {
//      if(method.getAnnotation(Pending.class) != null) {
//        list.add(method);
//      }
//    }
//
//    for (FrameworkMethod method : testClass.getAnnotatedMethods(Then.class)) {
//      if(method.getAnnotation(Pending.class) != null) {
//        list.add(method);
//      }
//    }
//    // TODO: refactor this method to something like the following
//    // addStepMethods(list, testClass, Given)
//    // addStepMethods(list, testClass, When)
//    // addStepMethods(list, testClass, Then)
//
//    return list;
//  }

  // TODO: figure out how to get this method to work
  // TODO: test this method
//  public void addStepMethods(List<FrameworkMethod> list, TestClass testClass, StepType stepType) {
//    for (FrameworkMethod method : testClass.getAnnotatedMethods(StepType.class)) {
//      if(method.getAnnotation(Pending.class) != null) {
//        list.add(method);
//      }
//    }
//  }

  // This method returns a list of all the Given-When-Then step methods in the StepDefinition.
  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    List<FrameworkMethod> list = new ArrayList<FrameworkMethod>();
    TestClass testClass = this.getTestClass();
    List<FrameworkMethod> methodList;
    String str;

    for (FrameworkMethod method : testClass.getAnnotatedMethods(Given.class)) {
      list.add(method);

      Given given = method.getAnnotation(Given.class);
      str = "Given " + given.value();
      methodList = map.get(str);
      if(methodList == null) {
        methodList = new ArrayList<FrameworkMethod>();
      }
      methodList.add(method);
      map.put(str, methodList);
    }

    for (FrameworkMethod method : testClass.getAnnotatedMethods(When.class)) {
      list.add(method);

      When when = method.getAnnotation(When.class);
      str = "When " + when.value();
      methodList = map.get(str);
      if(methodList == null) {
        methodList = new ArrayList<FrameworkMethod>();
      }
      methodList.add(method);
      map.put(str, methodList);
    }

    for (FrameworkMethod method : testClass.getAnnotatedMethods(Then.class)) {
      list.add(method);

      Then then = method.getAnnotation(Then.class);
      str = "Then " + then.value();
      methodList = map.get(str);
      if(methodList == null) {
        methodList = new ArrayList<FrameworkMethod>();
      }
      methodList.add(method);

      map.put(str, methodList);
    }

    return list;
  }

  // -------------------
  // TODO:

  // "I have $value"
  // - "int"
  // - "double"

  // @Given("I have $value")
  // public void given(int value) {

  // @Given("I have $value")
  // public void givenDbl(double value) {

  // givenMap, whenMap, thenMap
  // -------------------
  List<Object> params = new ArrayList<Object>();

  // Invokes a Step method
  @Override
  protected Statement methodBlock(final FrameworkMethod method) {

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // FrameworkMethod - Object invokeExplosively(Object target, Object... params)
        method.invokeExplosively(stepDef, params.toArray());
      }
    };
  }

  @Override
  protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);

    // if (method.getAnnotation(Ignore.class) != null) {   // <- BlockJUnit4ClassRunner's version
    if (method.getAnnotation(Pending.class) != null) {
      notifier.fireTestIgnored(description);
    }
    else {
      runLeaf(methodBlock(method), description, notifier);
    }
  }
}

