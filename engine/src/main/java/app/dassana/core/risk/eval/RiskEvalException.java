package app.dassana.core.risk.eval;

public class RiskEvalException extends RuntimeException{
  String ruleName;

  public String getRuleName() {
    return ruleName;
  }

  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  public RiskEvalException(String ruleName) {
    this.ruleName = ruleName;
  }

  public RiskEvalException(String message, String ruleName) {
    super(message);
    this.ruleName = ruleName;
  }

  public RiskEvalException(String message, Throwable cause, String ruleName) {
    super(message, cause);
    this.ruleName = ruleName;
  }

  public RiskEvalException(Throwable cause, String ruleName) {
    super(cause);
    this.ruleName = ruleName;
  }

  public RiskEvalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
      String ruleName) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.ruleName = ruleName;
  }
}
