package app.dassana.core.runmanager.launch.model;

public enum RunMode {
  TEST, //dassana will NOT post alerts to the custom eventbridge bus
  PROD //dassana will post alerts to the custom eventbridge bus
}
