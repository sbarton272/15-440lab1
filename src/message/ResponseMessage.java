package message;

public class ResponseMessage implements Message {

	private static final long serialVersionUID = 4038380303841252253L;
	private boolean mSuccess;
	private String mMessage;

	public ResponseMessage(boolean success, String message) {
		mSuccess = success;
		mMessage = message;
	}

	public ResponseMessage(boolean success) {
		this(success, "");
	}

	public ResponseMessage() {
		this(true, "");
	}

	public boolean isSuccess() {
		return mSuccess;
	}

	public boolean isFailure() {
		return !mSuccess;
	}

	public String getMessage() {
		return mMessage;
	}

}
