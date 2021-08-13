package app.dassana.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class StringyThings {

  public static ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());


  public static String removeNewLines(String string) {
    return string.replace("\n", "").replace("\r", "");
  }

  public static String getJsonFromYaml(String yaml) throws JsonProcessingException {
    ObjectMapper jsonWriter = new ObjectMapper();
    return jsonWriter.writeValueAsString(yamlReader.readValue(yaml, Object.class));

  }

}
