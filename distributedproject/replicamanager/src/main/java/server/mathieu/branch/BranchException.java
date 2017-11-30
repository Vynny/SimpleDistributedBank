package server.mathieu.branch;
import java.io.Serializable;

public class BranchException extends RuntimeException implements Serializable {

	public BranchException() {
	}

	public BranchException(String message) {
		super(message);
	}

	public BranchException(Throwable cause) {
		super(cause);
	}

	public BranchException(String message, Throwable cause) {
		super(message, cause);
	}

	public BranchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
