package worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import message.AliveRequest;
import message.AliveResponse;
import message.LaunchResponse;
import message.LaunchRequest;
import message.RemoveRequest;
import message.RemoveResponse;
import message.RequestMessage;
import message.ResponseMessage;
import migratableprocess.MigratableProcess;

public class Worker {

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.out.println("usage: Worker <listeningPort>");
			throw new Exception("Invalid Arguments");
		}

		// Create socket to listen on
		int port = Integer.parseInt(args[0]);
		ServerSocket listeningSoc = new ServerSocket(port);

		// Startup message
		System.out.println("Worker listening on " + InetAddress.getLocalHost()
				+ ":" + port);
		System.out.println("================================================");

		// Create message handler
		MessageHandler messageHandler = new MessageHandler();

		// Sit in main loop waiting to deal with requests
		while (!Thread.currentThread().isInterrupted()) {

			// Get something on the socket and push off to thread to deal with
			// it
			Socket connected = listeningSoc.accept();
			messageHandler.handleRequest(connected);

		}

		// Close socket on interrupt
		listeningSoc.close();
	}
}

class MessageHandler {

	private static final int THREAD_JOIN_TIME = 100;
	private Map<Integer, ThreadRunnablePair> mThreadsMap;

	public MessageHandler() {
		mThreadsMap = new HashMap<Integer, ThreadRunnablePair>();
	}

	public void handleRequest(Socket connected) {
		System.out.println("REQUEST RECIEVED: " + connected.getInetAddress()
				+ ":" + connected.getPort());

		try {

			// Open stream and read request
			ObjectInputStream objInput = new ObjectInputStream(
					connected.getInputStream());
			RequestMessage msg = (RequestMessage) objInput.readObject();

			// Parse out correct message and run command
			if (msg.isLaunch()) {
				handleLaunchRequest(connected, (LaunchRequest) msg);
			} else if (msg.isRemove()) {
				handleRemoveRequest(connected, (RemoveRequest) msg);
			} else if (msg.isAlive()) {
				handleIsAliveRequest(connected, (AliveRequest) msg);
			}

			// Close connections
			objInput.close();
			connected.close();

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("REQUEST ERROR");
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------

	private void handleLaunchRequest(Socket connected, LaunchRequest msg)
			throws IOException {

		// Catch launch event
		System.out.println("LAUNCH REQUEST");

		// Launch the given process on a new thread
		int pid = launch(msg.getProcess());

		// Respond with success message
		sendResponse(connected, new LaunchResponse(true, pid));

		// Tell user about response
		if (pid > 0) {
			System.out.println("LAUNCHED: " + pid);
		} else {
			System.out.println("LAUNCHED: ERROR " + pid);
		}

	}

	/**
	 * Create a new thread and start its runnable process
	 * 
	 * @param process
	 *            MigratableProcess
	 * @return process id int
	 */
	private int launch(MigratableProcess process) {
		Thread thread = new Thread(process);
		int pid = process.getPid();

		// Store reference to process
		mThreadsMap.put(pid, new ThreadRunnablePair(thread, process));

		thread.start();

		return pid;
	}

	// ------------------------------------------------------------------

	private void handleRemoveRequest(Socket connected, RemoveRequest msg)
			throws IOException {

		// Catch remove event
		int pid = msg.getPid();
		System.out.println("REMOVE REQUEST: " + pid + "");

		if (!mThreadsMap.containsKey(pid)) {

			// If pid not running on a thread return dead message
			sendResponse(connected, new RemoveResponse(true));
		} else {

			// Launch the given process on a new thread and generate response
			MigratableProcess process = remove(pid);
			RemoveResponse response;
			if (process != null) {
				response = new RemoveResponse(true, process);
			} else {
				response = new RemoveResponse(false);
			}
				
			// Respond with success message
			sendResponse(connected, response);

			// Tell user about response
			if (response.isSuccess()) {
				System.out.println("REMOVED: " + pid);
			} else {
				System.out.println("REMOVED: ERROR " + pid);
			}

		}
	}

	/**
	 * Stop the given process and remove the thread
	 * 
	 * @param pid
	 * @return
	 */
	private MigratableProcess remove(int pid) {
		ThreadRunnablePair pair = mThreadsMap.get(pid);
		MigratableProcess process = (MigratableProcess) pair.getRunnable();
		Thread thread = pair.getThread();

		// Suspend which terminates
		process.suspend();

		try {

			// Terminate the thread, join to ensure completed
			thread.join(THREAD_JOIN_TIME);

			// Response with the process
			return process;

		} catch (InterruptedException e) {

			// Respond with failure
			return null;
		}
	}

	// ------------------------------------------------------------------

	private void handleIsAliveRequest(Socket connected, AliveRequest msg) throws IOException {
		
		// Catch remove event
		int pid = msg.getPid();
		System.out.println("IS ALIVE REQUEST: " + pid + "");

		// Get process and thread
		ThreadRunnablePair pair = mThreadsMap.get(pid);
		
		AliveResponse response;
		if (pair == null) {

			// If process not running on a thread return dead message
			response = new AliveResponse(true, false);
		} else {
			
			// Extract thread
			Thread thread = pair.getThread();

			// Check if thread is alive
			if (thread.isAlive()) {
				
				// Send that it is alive
				response = new AliveResponse(true, true);
			} else {
				
				// Send that it is dead
				response = new AliveResponse(true, false);

			}
		}

		// Send decided upon response
		sendResponse(connected, response);
		
		// Tell user about response
		if (response.isSuccess()) {
			System.out.println("IS ALIVE: SUCCESS " + pid);
		} else {
			System.out.println("IS ALIVE: ERROR " + pid);
		}

	}

	// ------------------------------------------------------------------

	/**
	 * Write back response message on socket and close the socket
	 * 
	 * @param connected
	 * @param proccessDeadResponse
	 * @throws IOException
	 */
	private void sendResponse(Socket connected, ResponseMessage response)
			throws IOException {
		ObjectOutputStream workerOutStream = new ObjectOutputStream(
				connected.getOutputStream());
		workerOutStream.writeObject(response);
		workerOutStream.close();
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
