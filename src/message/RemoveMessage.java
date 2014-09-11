package message;

public class RemoveMessage extends RequestMessage {

	private static final long serialVersionUID = -3317488367302054417L;
	private int mPid;
	
	public RemoveMessage(int pid) {
		mPid = pid;
	}

	@Override
	public boolean isRemove() {
		return true;
	}
	
	public int getPid() {
		return mPid;
	}

}
