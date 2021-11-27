package app.dassana.core.workflowmanager.risk.eval;

public class RiskEvalException extends RuntimeException{
  String ruleName;

  public String getRuleName() {
    return ruleName;
  }

  public RiskEvalException(String message, Throwable cause, String ruleName) {
    super(message, cause);
    this.ruleName = ruleName;
  }

}
