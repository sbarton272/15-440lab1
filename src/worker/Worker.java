package worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import message.LaunchResponse;
import message.LaunchRequest;
import message.RemoveRequest;
import message.RemoveResponse;
import message.RequestMessage;
import message.ResponseMessage;
import migratableprocess.MigratableProcess;

/**
 * ProcessManager controls the threads being used to run the various processes
 * 
 * TODO cleanup print statements
 *
 * @author Spencer
 * 
 */
public class Worker {

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.out.println("usage: Worker <listeningPort>");
			throw new Exception("Invalid Arguments");
		}

		// Create socket to listen on
		int port = Integer.parseInt(args[0]);
		ServerSocket listeningSoc = new ServerSocket(port);

		System.out.println("Worker listening on " + InetAddress.getLocalHost()
				+ ":" + port);

		// Create message handler
		MessageHandler messageHandler = new MessageHandler();

		// Sit in main loop waiting to deal with requests
		while (!Thread.currentThread().isInterrupted()) {

			// Get something on the socket and push off to thread to deal with
			// it
			Socket connected = listeningSoc.accept();
			messageHandler.handleMessage(connected);

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

	public void handleMessage(Socket connected) {
		System.out.println("Command recieved from "
				+ connected.getInetAddress() + ": " + connected.getPort());

		// Read in object
		try {

			// Open stream and read request
			ObjectInputStream objInput = new ObjectInputStream(
					connected.getInputStream());
			RequestMessage msg = (RequestMessage) objInput.readObject();

			// Parse out correct message and run command
			if (msg.isLaunch()) {

				// Catch launch event
				System.out.println("Recieved Launch Request");

				// Launch the given process on a new thread
				int pid = launch(((LaunchRequest) msg).getProcess());

				// Respond with success message
				sendResponse(connected, new LaunchResponse(true, pid));

			} else if (msg.isRemove()) {

				// Catch remove event
				int pid = ((RemoveRequest) msg).getPid();
				System.out.println("Recieved Remove Request " + pid);

				if (!mThreadsMap.containsKey(pid)) {

					// If pid not running on a thread return dead message
					sendResponse(connected, new RemoveResponse(true));
				} else {

					// Launch the given process on a new thread and generate response
					RemoveResponse response = remove(pid);

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

			// Close connections
			objInput.close();
			connected.close();

		} catch (IOException | ClassNotFoundException e) {
			// TODO not well handled
			e.printStackTrace();
		}
	}

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

		System.out.println("LAUNCHED " + pid);

		return pid;
	}

	/**
	 * Stop the given process and remove the thread
	 * 
	 * @param pid
	 * @return
	 */
	private RemoveResponse remove(int pid) {
		ThreadRunnablePair pair = mThreadsMap.get(pid);
		MigratableProcess process = (MigratableProcess) pair.getRunnable();
		Thread thread = pair.getThread();

		// Suspend which terminates
		process.suspend();

		try {

			// Terminate the thread, join to ensure completed
			thread.join(THREAD_JOIN_TIME);

			// Response with the process
			return new RemoveResponse(true, process);

		} catch (InterruptedException e) {

			// Respond with failure
			return new RemoveResponse(false);
		}
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
