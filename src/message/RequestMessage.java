package message;

public abstract class RequestMessage implements Message {

	private static final long serialVersionUID = 6340377898025061617L;

	/*
	 * Methods act to identify request type
	 */
	public boolean isLaunch() {
		return false;
	}
	
	public boolean isRemove() {
		return false;
	}
	
	public boolean isAlive() {
		return false;
	}
		
}
