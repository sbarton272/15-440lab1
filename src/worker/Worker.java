package worker;
import helper.Serializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import message.LaunchMessage;
import message.Message;
import migratableprocess.MigratableProcess;

/**
 * ProcessManager controls the threads being used to run the various processes
 * 
 * @author Spencer
 *
 */
public class Worker {

	private static final int THREAD_JOIN_TIME = 100;
	private Map<Integer, ThreadRunnablePair> mThreadsMap;
	
	public Worker() {
		mThreadsMap = new HashMap<Integer, ThreadRunnablePair>(); 
	}
	
	public static void main(String [ ] args) {
		
		if (args.length != 1) {
			System.out.println("usage: Worker <listeningPort>");
			throw new Exception("Invalid Arguments");
		}
		
		ServerSocket listeningSoc = new ServerSocket(Integer.parseInt(args[0]));
		
		// Sit in main loop waiting to deal with requests
		while (true) {
            
    		// Get something on the socket and push off to thread to deal with it
            Socket connected = listeningSoc.accept();
            new commandParser(connected).start();
		}
		
    }
}

class commandParser extends Thread {

	public commandParser(Socket connected) {
		System.out.println("Command recieved from " + connected.getInetAddress() + ": " + connected.getPort());

		// Read in object
		ObjectInputStream objInput = new ObjectInputStream(connected.getInputStream());
		Message msg = (Message) objInput.readObject();
		objInput.close();
		
		// Parse out correct message and run command
		
		// Catch launch event
		if (msg instanceof LaunchMessage) {
			
		}
		
		// Contains pid and process
		
		// Catch remove event
		
		// Catch migrate event
		
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
