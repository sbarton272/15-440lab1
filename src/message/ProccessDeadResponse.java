package message;


public class ProccessDeadResponse extends Response {

	private static final long serialVersionUID = 6344213116880521650L;
	private int mPid;
	
	public ProccessDeadResponse(int pid) {
		super(false);
		mPid = pid;
	}
	
	public int getPid() {
		return mPid;
	}

}
