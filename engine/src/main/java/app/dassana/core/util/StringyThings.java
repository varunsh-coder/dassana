package app.dassana.core.util;

public class StringyThings {

  public static String removeNewLines(String string) {
    return string.replace("\n", "").replace("\r", "");
  }

}
