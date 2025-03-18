package identity.am.automation.exception;

/**
 * Exception thrown for workflow-related errors
 */
public class WorkflowException extends Exception {
    public WorkflowException(String message) {
        super(message);
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
