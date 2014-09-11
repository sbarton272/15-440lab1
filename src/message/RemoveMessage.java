package message;

public class RemoveMessage implements RequestMessage {

	private static final long serialVersionUID = -3317488367302054417L;
	private int mPid;
	
	public RemoveMessage(int pid) {
		mPid = pid;
	}

	public int getPid() {
		return mPid;
	}

}
