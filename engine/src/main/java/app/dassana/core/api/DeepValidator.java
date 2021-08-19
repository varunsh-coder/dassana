package app.dassana.core.api;

import app.dassana.core.util.PolicyValidator;

import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * This validator checks if the content in the "content" dir is kosher or not
 */
@Singleton
public class DeepValidator {


  public void validate() throws IOException {
    String content = Thread.currentThread().getContextClassLoader().getResource("content").getFile();
    PolicyValidator validator = new PolicyValidator();
    validator.loadYaml(content + "/schemas/policy-classification/policy-classification.yaml");
    validator.processFiles(content + "/workflows");
  }


}
