package app.dassana.core.runmanager.launch.model;

public class Message {

  private Severity severity = Severity.ERROR;
  private String msg;

  public Message() { }

  public Message(String message) {
    this.msg = message;
  }

  public Message(String message, Severity severity) {
    this.msg = message;
    this.severity = severity;
  }

  public Severity getSeverity() {
    return severity;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public String toString() {
    return "Message{" +
            "severity=" + severity +
            ", msg='" + msg + '\'' +
            '}';
  }
}