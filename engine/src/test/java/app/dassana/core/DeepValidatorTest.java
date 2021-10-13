package app.dassana.core;


import static org.junit.jupiter.api.Assertions.fail;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.io.IOException;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
public class DeepValidatorTest {

  @Inject
  app.dassana.core.api.DeepValidator deepValidator;

  @Test
  void validate() {
    try {
      deepValidator.validate();
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

}
