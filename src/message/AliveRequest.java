package message;

public class AliveRequest extends RequestMessage {

	private static final long serialVersionUID = -4710944800783583511L;
	private int mPid;
	
	public AliveRequest(int pid) {
		mPid = pid;
	}

	@Override
	public boolean isAlive() {
		return true;
	}
	
	public int getPid() {
		return mPid;
	}

}
