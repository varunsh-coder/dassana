package app.dassana.core.util;

import com.google.gson.Gson;
import io.micronaut.context.annotation.Factory;
import javax.inject.Singleton;

@Factory
public class JsonyThings {

  @Singleton
  Gson getGson() {
    return new Gson();
  }


}
