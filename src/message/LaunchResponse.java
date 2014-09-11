package message;


public class LaunchResponse extends ResponseMessage {

	private static final long serialVersionUID = 6344213116880521650L;
	private int mPid;
	
	public LaunchResponse(boolean success, int pid) {
		super(success);
		mPid = pid;
	}
	
	public int getPid() {
		return mPid;
	}

}
