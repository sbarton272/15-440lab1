package message;

public abstract class Response implements Message {

	private static final long serialVersionUID = 4038380303841252253L;
	private boolean mSuccess;

	public Response(boolean success) {
		mSuccess = success;
	}

	public Response() {
		this(true);
	}

	public boolean isSuccess() {
		return mSuccess;
	}

	public boolean isFailure() {
		return !mSuccess;
	}

}
