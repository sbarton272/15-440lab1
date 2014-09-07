import java.util.HashMap;
import java.util.Map;

/**
 * ProcessManager controls the threads being used to run the various processes
 * 
 * TODO
 * - Get grep to run to completion
 * - Catch death incident
 * - Cleanup completed threads
 * 
 * @author Spencer
 *
 */
public class ProcessManager {

	private Map<Integer, java.lang.Thread> threadsMap;
	
	public ProcessManager() {
		threadsMap = new HashMap<Integer, Thread>(); 
	}
	
	/**
	 * Create a new thread and start its runnable process
	 * 
	 * @param process MigratableProcess
	 * @param argStrs Array of string arguments for process
	 * @return process id int
	 */
	public int launch(MigratableProcess process) {
		Thread thread = new Thread(process);
		int pid = thread.hashCode();
		
		// Store reference to thread
		threadsMap.put(pid, thread);
		
		thread.start();
		return pid;
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
