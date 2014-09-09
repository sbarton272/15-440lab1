package message;

public class ResponseMessage implements Message {

	private static final long serialVersionUID = 4038380303841252253L;
	private boolean mSuccess;
	
	public ResponseMessage(boolean success) {
		mSuccess = success;
	}
	
	public boolean isSuccess() {
		return mSuccess;
	}
	
	public boolean isFailure() {
		return !mSuccess;
	}
	
}
