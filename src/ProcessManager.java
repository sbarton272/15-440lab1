import java.util.Map;


/**
 * ProcessManager controls the threads being used to run the various processes
 * 
 * @author Spencer
 *
 */
public class ProcessManager {

	private Map<java.lang.Thread> threadsMap;
	
	public ProcessManager() {
		
	}
	
	/**
	 * Create a new thread and start its runnable process
	 * 
	 * @param process MigratableProcess
	 * @param argStrs Array of string arguments for process
	 * @return process id int
	 */
	public int launch(MigratableProcess process) {
		
	}
	
	/**
	 * Stop the given process and remove the thread
	 * 
	 * @param pid
	 */
	public void remove(int pid) {
		
	}
	
	/**
	 * Suspend the given process, serialize the process and transfer to a new thread 
	 * 
	 * @param pid
	 */
	public void migrate(int pid) {
		
	}
	
}
