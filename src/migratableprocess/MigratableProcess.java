package migratableprocess;
import java.io.Serializable;

public abstract class MigratableProcess implements Serializable, Runnable {
	
	private static final long serialVersionUID = -3105461466732257272L;
	protected int mPid = -1;
	
	/**
	 * Required by the Runnable interface. This is run when the thread starts.
	 */
	public abstract void run();
	
	/**
	 * Suspend stops the run loop and prepares the process for migration or termination 
	 */
	public abstract void suspend();
	
	/**
	 * Create a string representation of the object including the starting argument values.
	 * 
	 * @return representation of object 
	 */
	public abstract String toString();
	
	/**
	 * Set process id if not already set. Note pid > 0.
	 * @return pid
	 */
	public int setPid(int pid) {
		if (mPid == -1) {
			mPid = pid;
		}
		return mPid;
	}
	
	/**
	 * Get process id
	 */
	public int getPid() {
		return mPid;
	}
	
}
