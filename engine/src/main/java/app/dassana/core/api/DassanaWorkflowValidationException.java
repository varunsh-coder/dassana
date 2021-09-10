package app.dassana.core.api;

import app.dassana.core.launch.model.Message;

import java.util.LinkedList;
import java.util.List;

public class DassanaWorkflowValidationException extends RuntimeException {

  List<Message> messages;

  public List<Message> getMessages() {
    if (messages == null) {
      messages = new LinkedList<>();
    }
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

  public DassanaWorkflowValidationException() {
    super();
  }

  public DassanaWorkflowValidationException(String message) {
    super(message);
  }
}
