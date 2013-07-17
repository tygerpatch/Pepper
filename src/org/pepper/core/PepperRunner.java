package org.pepper.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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

  public FrameworkMethod getStepMethod(String line, Class<? extends Annotation> annotationClass) {
    // TODO: create unit tests for this method
    String step = null;
    Class<?>[] parameterTypes;

    for (FrameworkMethod frameworkMethod : getTestClass().getAnnotatedMethods(annotationClass)) {
      if (frameworkMethod.getAnnotation(Pending.class) == null) {

        // TODO: figure out a way to reduce the duplication here
        if (frameworkMethod.getAnnotation(annotationClass) instanceof Given) {
          step = (frameworkMethod.getAnnotation(Given.class)).value();
        }
        else if (frameworkMethod.getAnnotation(annotationClass) instanceof When) {
          step = (frameworkMethod.getAnnotation(When.class)).value();
        }
        else if (frameworkMethod.getAnnotation(annotationClass) instanceof Then) {
          step = (frameworkMethod.getAnnotation(Then.class)).value();
        }

        parameterTypes = frameworkMethod.getMethod().getParameterTypes();

        if (checkMethod(parameterTypes, step, line)) {
          return frameworkMethod;
        }
      }
    }

    return null;
  }

  public boolean checkMethod(Class<?>[] parameterTypes, String step, String line) {
    params.clear();

    StringTokenizer stepTokenizer = new StringTokenizer(step);
    StringTokenizer lineTokenizer = new StringTokenizer(line);

    String stepToken, lineToken;

    // iterate over each word in method and line
    while (stepTokenizer.hasMoreTokens() && lineTokenizer.hasMoreTokens()) {
      stepToken = stepTokenizer.nextToken();
      lineToken = lineTokenizer.nextToken();

      // if both words don't match
      if (!stepToken.equals(lineToken)) {
        // if word in method isn't a placeholder, continue with next method
        if (!stepToken.startsWith("$")) {
          return false;
        }

        Object obj = parseArgument(lineToken);
        params.add(obj);
        String str = "";

        if ("java.lang.Integer".equals(obj.getClass().getName())) {
          str = "int";
        }
        else if ("java.lang.Double".equals(obj.getClass().getName())) {
          str = "double";
        }
        else if ("java.lang.Boolean".equals(obj.getClass().getName())) {
          str = "boolean";
        }

        // if we've read more arguments than the method takes, continue with next method
        if (params.size() > parameterTypes.length) {
          // TODO: demonstrate purpose of this test
          return false;
        }

        // if the given argument type does not match up with the expected argument, continue with next method
        if (!parameterTypes[params.size() - 1].getCanonicalName().equals(str)) {
          return false;
        }
      }
    }

    if (!stepTokenizer.hasMoreTokens() && !lineTokenizer.hasMoreTokens()) {
      return true;
    }

    return false;
  }

  public static Object parseArgument(String arg) {
    // if the given argument type does not match up with the expected argument, continue with next method
    try {
      return Integer.valueOf(arg);
    }
    catch (NumberFormatException intNumberFormat) {
      // TODO: should exception be logged
      try {
        return Double.valueOf(arg);
      }
      catch (NumberFormatException dblNumberFormat) {
        // TODO: should exception be logged
        return Boolean.valueOf(arg);
      }
    }
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

            if(line.startsWith("Given")) {
              method = getStepMethod(line.substring(5), Given.class);

              if (method == null) {
                System.out.println(generateStub(line));
              }
              else {
                listener.setLine(line);
                PepperRunner.this.runChild(method, notifier);
              }
            }
            else if(line.startsWith("When")) {
              method = getStepMethod(line.substring(4), When.class);

              if (method == null) {
                System.out.println(generateStub(line));
              }
              else {
                listener.setLine(line);
                PepperRunner.this.runChild(method, notifier);
              }
            }
            else if(line.startsWith("Then")) {
              method = getStepMethod(line.substring(4), Then.class);

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
              else if(line.startsWith("Scenario Template:")) {
                scenarioTemplate(scanner, listener, notifier);
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

  private void scenarioTemplate(Scanner scanner, PepperRunnerListener listener, RunNotifier notifier) {
    // read lines in Scenario Template
    String line;
    List<String> scenarioTemplate = new ArrayList<String>();

    while (scanner.hasNextLine()) {
      line = scanner.nextLine().trim();
      if (line.equals("Content Table:")) {
        break; // from while loop
      }
      scenarioTemplate.add(line);
    }

    // read Content Table's "header"
    Map<String, List<String>> contentTable = new HashMap<String, List<String>>();
    List<String> keys = new ArrayList<String>();
    line = scanner.nextLine().trim();
    for (String key : line.split("|\\w|")) {
      key = key.trim();
      if (!key.isEmpty() && !"|".equals(key)) {
        contentTable.put(key, new ArrayList<String>());
        keys.add(key);
      }
    }

    // read Content Table's "body"
    String[] strArray;
    String key;
    List<String> list;
    int column;
    StringBuilder strBuilder = null;
    String str;
    int numRows = 0;
    while (scanner.hasNextLine()) {
      line = scanner.nextLine().trim();

      if (!line.startsWith("|")) {
        break; // from while loop
      }

      numRows++;
      column = 0;
      strBuilder = null;
      for (char ch : line.toCharArray()) {
        if (ch == '|') {
          if (strBuilder != null) {
            str = strBuilder.toString().trim();

            key = keys.get(column);

            list = contentTable.get(key);
            list.add(str);
            column++;
          }
          strBuilder = new StringBuilder();
        }
        else {
          strBuilder.append(ch);
        }
      }

    } // end of while (scanner.hasNextLine()) {

    // run Scenario Template
    int row = 0;
    FrameworkMethod method;

    while (row < numRows) {
      for (String strLine : scenarioTemplate) {
        for (String strKey : contentTable.keySet()) {
          list = contentTable.get(strKey);
          strLine = strLine.replaceAll("<" + strKey + ">", list.get(row));
        }

        if (strLine.startsWith("Given")) {
          method = getStepMethod(strLine.substring(5), Given.class);

          if (method == null) {
            System.out.println(generateStub(strLine));
          }
          else {
            listener.setLine(strLine);
            PepperRunner.this.runChild(method, notifier);
          }
        }
        else if (strLine.startsWith("When")) {
          method = getStepMethod(strLine.substring(4), When.class);

          if (method == null) {
            System.out.println(generateStub(strLine));
          }
          else {
            listener.setLine(strLine);
            PepperRunner.this.runChild(method, notifier);
          }
        }
        else if (strLine.startsWith("Then")) {
          method = getStepMethod(strLine.substring(4), Then.class);

          if (method == null) {
            System.out.println(generateStub(strLine));
          }
          else {
            listener.setLine(strLine);
            PepperRunner.this.runChild(method, notifier);
          }
        }
      }
      System.out.println();
      row++;
    }
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

  @Override
  protected List<FrameworkMethod> getChildren() {
    List<Class<? extends Annotation>> annotationClasses = new ArrayList<Class<? extends Annotation>>();

    annotationClasses.add(Given.class);
    annotationClasses.add(When.class);
    annotationClasses.add(Then.class);

    List<FrameworkMethod> frameworkMethods = new ArrayList<FrameworkMethod>();
    TestClass testClass = this.getTestClass();

    for (Class<? extends Annotation> annotationClass : annotationClasses) {
      for (FrameworkMethod method : testClass.getAnnotatedMethods(annotationClass)) {
        if (method.getAnnotation(Pending.class) != null) {
          frameworkMethods.add(method);
        }
      }
    }

    return frameworkMethods;
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
