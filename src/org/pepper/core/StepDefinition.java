package org.pepper.core;

import java.io.File;

import org.junit.runner.RunWith;

@RunWith(PepperRunner.class)
public abstract class StepDefinition {

  private String featureName;

  // By default, the name of the feature file should be the same as the
  // StepDefinition subclass, only snake cased.
  public String getFeatureName() {
    if (featureName == null) {
      String className = this.getClass().getName();

      className = className.substring(className.lastIndexOf('.') + 1);
      className = className.replace("StepDefinition", "");

      className = StringUtils.snakecase(className);

      setFeatureName(className);
    }

    return featureName;
  }

  // Allows User to customize the name of the feature file.
  public void setFeatureName(String featureName) {
    this.featureName = featureName;
  }

  private String featureExtension;

  // By default, feature files will have an extension of ".feature"
  public String getFeatureExtension() {
    if (featureExtension == null) {
      featureExtension = ".feature";
    }

    if (!featureExtension.startsWith(".")) {
      featureExtension = "." + featureExtension;
    }

    return featureExtension;
  }

  // Allow the User to customize the feature file's extension name.
  public void setFeatureExtension(String featureExtension) {
    this.featureExtension = featureExtension;
  }

  private String getFeaturesFolder(File file) {
    for (File f : file.listFiles()) {
      if (f.getAbsolutePath().endsWith("features")) {
        return f.getAbsolutePath();
      }
      else if (f.isDirectory()) {
        String featuresFolder = getFeaturesFolder(f);
        if (featuresFolder != null) {
          return featuresFolder;
        }
      }
    }
    return null;
  }

  // By default will look for feature files in the first folder it finds named "feature".
  // Returns null if folder is not found.
  public String getFeaturesFolder() {
    String featuresFolder = getFeaturesFolder(new File("."));

    if (featuresFolder == null) {
      return null;
    }

    return new File("").getAbsolutePath() + featuresFolder.substring(featuresFolder.lastIndexOf(".") + 1);
    // Note: If you use the empty string "" for the File's pathname, it will return a null for its listFiles() method
  }
}
