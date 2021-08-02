package app.dassana.core.launch.model;

public class Message {

  private app.dassana.core.launch.model.severity severity;
  private String msg;

  public severity getSeverity() {
    return severity;
  }

  public void setSeverity(severity severity) {
    this.severity = severity;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

}