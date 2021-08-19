package app.dassana.core;


import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class DeepValidator {

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
