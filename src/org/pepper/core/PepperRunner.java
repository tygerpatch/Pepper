package org.pepper.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
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

//  public FrameworkMethod getStep(String line, Annotation stepType) {
//    TestClass testClass = this.getTestClass();
//
//    for (FrameworkMethod frameworkMethod : testClass.getAnnotatedMethods(stepType.class)) {
//      if (frameworkMethod.getAnnotation(Pending.class) == null) {
//
//        Given given = method.getAnnotation(Given.class);
//        str = "Given " + given.value();
//        methodList = map.get(str);
//        if(methodList == null) {
//          methodList = new ArrayList<FrameworkMethod>();
//        }
//        methodList.add(method);
//        map.put(str, methodList);
//
//      }
//    }
//  }

//  public FrameworkMethod extractMethod(String line) {
//    List<FrameworkMethod> frameworkMethods = new ArrayList<FrameworkMethod>();
//    List<String> steps = new ArrayList<String>();
//
//    if(line.startsWith("Given")) {
//      // return getStep(line);
//      frameworkMethods = getStepMethods(Given.class);
//    }
//    else if(line.startsWith("When")) {
//      frameworkMethods = getStepMethods(When.class);
//    }
//    else if(line.startsWith("Then")) {
//      frameworkMethods = getStepMethods(Then.class);
//    }
//
//    StringTokenizer stepTokenizer, lineTokenizer;
//    String stepToken, lineToken;
//    FrameworkMethod frameworkMethod;
//    Method method;
//
//    outer:
//    for (int i = 0; (i < frameworkMethods.size()) && (i < steps.size()); i++) {
//      params.clear();
//
//      frameworkMethod = frameworkMethods.get(i);
//
//      stepTokenizer = new StringTokenizer(steps.get(i));
//      lineTokenizer = new StringTokenizer(line);
//
//      // iterate over each word in method and line
//      while(stepTokenizer.hasMoreTokens() && lineTokenizer.hasMoreTokens()) {
//        stepToken = stepTokenizer.nextToken();
//        lineToken = lineTokenizer.nextToken();
//
//        // if both words don't match
//        if(!stepToken.equals(lineToken)) {
//          // if word in method isn't a placeholder, continue with next method
//          if(!stepToken.startsWith("$")) {
//            continue outer;
//          }
//
//          // if the given argument type does not match up with the expected argument, continue with next method
//          try {
//            params.add(Integer.valueOf(stepToken));
//            if("int" != frameworkMethod.getMethod().getParameterTypes()[params.size()].getCanonicalName()) {
//              continue outer;
//            }
//          }
//          catch (NumberFormatException intNumberFormat) {
//            // TODO: should exception be logged
//            try {
//              params.add(Double.valueOf(stepToken));
//              if("double" != frameworkMethod.getMethod().getParameterTypes()[params.size()].getCanonicalName()) {
//                continue outer;
//              }
//            }
//            catch (NumberFormatException dblNumberFormat) {
//              // TODO: should exception be logged
//              params.add(Boolean.valueOf(stepToken));
//              if("boolean" != frameworkMethod.getMethod().getParameterTypes()[params.size()].getCanonicalName()) {
//                continue outer;
//              }
//            }
//          }
//        } // end of if(!stepToken.equals(lineToken)) {
//      } // end of while(stepTokenizer.hasMoreTokens() && lineTokenizer.hasMoreTokens()) {
//
//      if(!stepTokenizer.hasMoreTokens() && !lineTokenizer.hasMoreTokens()) {
//        return frameworkMethod;
//      }
//    } // for(FrameworkMethod frameworkMethod : list) {
//
//    return null;
//  }

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

  public static String generateStub(String line) {
    StringBuilder strBuilder = new StringBuilder();

    // System.out.println("@Pending");
    strBuilder.append("@Pending\n");

    // Given = 5 characters long
    // When = 4 characters long
    // Then = 4 characters long
    int length = line.startsWith("Given") ? 5 : 4;

    // System.out.println("@" + line.substring(0, length) + "(\"" + line.substring(length + 1) + "\")");
    strBuilder.append("@");
    strBuilder.append(line.substring(0, length));
    strBuilder.append("(\"");
    strBuilder.append(line.substring(length + 1));
    strBuilder.append("\")\n");

    // System.out.println("public void " + StringUtils.camelCase(line) + "() {");
    strBuilder.append("public void ");
    strBuilder.append(StringUtils.camelCase(line));
    strBuilder.append("() {\n");

    // System.out.println("}\n");
    strBuilder.append("}\n");

    // Note: This method returns a String so it can be tested.
    // I left the System.out.println statements, because they're slightly easier to read.

    return strBuilder.toString();
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

            if (line.startsWith("Given") || line.startsWith("When") || line.startsWith("Then")) {
              method = extractMethod(line);

              if (method == null) {
                System.out.println(generateStub(line));
              }
              else {
                listener.setLine(line);
                PepperRunner.this.runChild(method, notifier);
              }
            }
            else {
              System.out.println(line);
              if (line.startsWith("Scenario:")) {
                newStepDefinition();
              }
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
