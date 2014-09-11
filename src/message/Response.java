package message;

public class Response implements Message {

	private static final long serialVersionUID = 4038380303841252253L;
	private boolean mSuccess;
	private String mMessage;

	public Response(boolean success, String message) {
		mSuccess = success;
		mMessage = message;
	}

	public Response(boolean success) {
		this(success, "");
	}

	public Response() {
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
