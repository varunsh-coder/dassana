package app.dassana.core;


import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MicronautTest
public class DeepValidator {

  @Inject
  app.dassana.core.api.DeepValidator deepValidator;


  @Test
  @Disabled
  void validate() {
    deepValidator.validate();

  }


}
