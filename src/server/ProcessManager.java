package server;
import helper.Serializer;

import java.util.HashMap;
import java.util.Map;

import migratableprocess.MigratableProcess;

/**
 * ProcessManager controls the threads being used to run the various processes
 * 
 * TODO
 * - Migrate
 * - Remove
 * - Catch death incident
 * - Cleanup completed threads
 * - Name member variables with m in front
 * 
 * @author Spencer
 *
 */
public class ProcessManager {

	private static final int THREAD_JOIN_TIME = 100;
	private Map<Integer, ThreadRunnablePair> mThreadsMap;
	
	public ProcessManager() {
		mThreadsMap = new HashMap<Integer, ThreadRunnablePair>(); 
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
		
		// Store reference to process
		mThreadsMap.put(pid, new ThreadRunnablePair(thread, process));
		
		thread.start();
		return pid;
	}
	
	/**
	 * Stop the given process and remove the thread
	 * 
	 * @param pid
	 */
	public void remove(int pid) {
		ThreadRunnablePair pair = mThreadsMap.get(pid);
		MigratableProcess process = (MigratableProcess) pair.getRunnable();
		Thread thread = pair.getThread();
		
		// Suspend which terminates
		System.out.println("SUSPENDING");
		process.suspend();
		System.out.println("SUSPENDING DONE");
		
		// Terminate the thread, join to ensure completed
		try {
			thread.join(THREAD_JOIN_TIME);
			System.out.println("REMOVED: " + pid);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Suspend the given process, serialize the process and transfer to a new thread 
	 * 
	 * @param pid
	 */
	public void migrate(int pid) {
		
		// Extract process and thread from pid
		ThreadRunnablePair pair = mThreadsMap.get(pid);
		MigratableProcess process = (MigratableProcess) pair.getRunnable();
		Thread thread = pair.getThread();
		
		// Suspend and serialize
		process.suspend(); // TODO why not store suspend flag
		
		// Serialize (and generate unique serialization filename)
		String uid = Integer.toString(process.hashCode());
		Serializer serializer = new Serializer(uid);
		serializer.serialize(process);
		
		// Terminate the thread, join to ensure completed
		try {
			thread.join(THREAD_JOIN_TIME);
			System.out.println("REALLY DONE");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Deserialize the process and start a new thread
		process = (MigratableProcess) serializer.deserialize();
		Thread newThread = new Thread(process);
		System.out.println("RESTART");
		
		// Store reference to process
		mThreadsMap.put(pid, new ThreadRunnablePair(newThread, process));

		newThread.start(); // TODO does not seem to run the process

	}
	
	/**
	 * Used to store thread and its runnable in a tuple
	 */
	private class ThreadRunnablePair {
		private Thread mThread;
		private Runnable mRunnable;
		
		public ThreadRunnablePair(Thread thread, Runnable runnable) {
			mThread = thread;
			mRunnable = runnable;
		}
		
		public Thread getThread() {
			return mThread;
		}
		
		public Runnable getRunnable() {
			return mRunnable;
		}
	}
	
}
