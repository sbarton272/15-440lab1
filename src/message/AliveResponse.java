package message;


public class AliveResponse extends ResponseMessage {

	private static final long serialVersionUID = -2804742247204608267L;
	private boolean mAlive;
	
	public AliveResponse(boolean success, boolean alive) {
		super(success);
		mAlive = alive;
	}
	
	public boolean isAlive() {
		return mAlive;
	}

}
