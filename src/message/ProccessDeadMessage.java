package message;


public class ProccessDeadMessage extends ResponseMessage {

	private static final long serialVersionUID = 6344213116880521650L;
	private int mPid;
	
	public ProccessDeadMessage(int pid) {
		super(false);
		mPid = pid;
	}
	
	public int getPid() {
		return mPid;
	}

}
