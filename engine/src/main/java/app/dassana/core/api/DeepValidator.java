package app.dassana.core.api;

import javax.inject.Singleton;

/**
 * This validator checks if the content in the "content" dir is kosher or not
 */
@Singleton
public class DeepValidator {


  public void validate() {

    String content = Thread.currentThread().getContextClassLoader().getResource("content").getFile();

    System.out.println("Going to validate directory".concat(content));
    //do validation here and if validation fails, throw exception like this
    throw new ValidationException("this ain't right");

  }


}
