package worker;

import helper.Serializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import message.LaunchMessage;
import message.Message;
import message.ProcessDeadResponse;
import message.RemoveMessage;
import message.RequestMessage;
import message.Response;
import migratableprocess.MigratableProcess;

/**
 * ProcessManager controls the threads being used to run the various processes
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
			System.out.println("CLOSED? " + connected.isClosed());
			
			ObjectInputStream objInput = new ObjectInputStream(
					connected.getInputStream());
			RequestMessage msg = (RequestMessage) objInput.readObject();
			
			System.out.println("CLOSED? " + connected.isClosed());

			// Parse out correct message and run command
			if (msg.isLaunch()) {

				// Catch launch event
				System.out.println("Recieved Launch Message");

				// Launch the given process on a new thread
				launch(((LaunchMessage) msg).getProcess());
				
				// Respond with success message
				sendResponse(connected, new Response());

			}
			else if (msg.isRemove()) {

				// Catch remove event
				int pid = ((RemoveMessage) msg).getPid();
				System.out.println("Recieved Remove Message " + pid);

				// If pid not running on a thread return dead message
				if (!mThreadsMap.containsKey(pid)) {
					sendResponse(connected, new ProcessDeadResponse(pid));
				} else {
				
				// Launch the given process on a new thread
				boolean success = remove(pid);

				// Respond with success message
				sendResponse(connected, new Response(success));
				
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
	private void sendResponse(Socket connected, Response response)
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
	public int launch(MigratableProcess process) {
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
	public boolean remove(int pid) {
		ThreadRunnablePair pair = mThreadsMap.get(pid);
		MigratableProcess process = (MigratableProcess) pair.getRunnable();
		Thread thread = pair.getThread();

		// Suspend which terminates
		process.suspend();

		// Terminate the thread, join to ensure completed
		try {
			thread.join(THREAD_JOIN_TIME);
			System.out.println("REMOVED: " + pid);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * Suspend the given process, serialize the process and transfer to a new
	 * thread
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
