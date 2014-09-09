package migratableprocess;
import java.io.Serializable;

public interface MigratableProcess extends Serializable, Runnable {
	
	/**
	 * Required by the Runnable interface. This is run when the thread starts.
	 */
	public void run();
	
	/**
	 * Suspend stops the run loop and prepares the process for migration or termination 
	 */
	public void suspend();
	
	/**
	 * Create a string representation of the object including the starting argument values.
	 * 
	 * @return representation of object 
	 */
	public String toString();
	
}
